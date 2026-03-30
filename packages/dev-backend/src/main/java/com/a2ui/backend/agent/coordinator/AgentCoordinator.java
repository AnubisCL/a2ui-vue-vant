package com.a2ui.backend.agent.coordinator;

import com.a2ui.backend.agent.general.GeneralAgent;
import com.a2ui.backend.guardrail.InputGuardrail;
import com.a2ui.backend.guardrail.OutputGuardrail;
import com.a2ui.backend.guardrail.ValidationResult;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.SurfaceMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agent Coordinator - main entry point for message processing.
 * Integrates GeneralAgent, InputGuardrail, OutputGuardrail, and A2uiMessage parsing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentCoordinator {

    private final GeneralAgent generalAgent;
    private final InputGuardrail inputGuardrail;
    private final OutputGuardrail outputGuardrail;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process user message through the agent pipeline.
     *
     * Pipeline:
     * 1. Input validation
     * 2. Input sanitization
     * 3. GeneralAgent.chat() - LLM processing with output buffering
     * 4. Parse accumulated output to A2uiMessage
     */
    public Flux<A2uiMessage> processMessage(String message, String sessionId) {
        // 1. Input validation
        ValidationResult inputResult = inputGuardrail.validate(message);
        if (!inputResult.isSuccess()) {
            log.warn("输入校验失败: {}", inputResult.getMessage());
            return Flux.just(createErrorMessage(inputResult.getMessage()));
        }

        // 2. Sanitize input
        String sanitizedMessage = inputGuardrail.sanitize(message);

        // 3. Get memory ID from session
        Long memoryId = sessionId.hashCode() & 0xFFFFFFFFL;

        // 4. Create output buffer for accumulating LLM output
        A2uiOutputBuffer buffer = new A2uiOutputBuffer();

        // 5. Call GeneralAgent and accumulate output
        return generalAgent.chat(sanitizedMessage, memoryId)
            .collectList()
            .flatMapMany(chunks -> {
                // Accumulate all chunks
                for (String chunk : chunks) {
                    buffer.append(chunk);
                }

                // Get all complete messages
                List<A2uiMessage> messages = buffer.drainMessages();

                // Handle any remaining text as a Text component
                String remainingText = buffer.getRemainingText();
                if (!remainingText.isBlank()) {
                    messages.add(createTextMessage(remainingText));
                }

                // If no messages at all, create a surface message
                if (messages.isEmpty()) {
                    messages.add(SurfaceMessage.of("main", "A2UI Surface"));
                }

                return Flux.fromIterable(messages);
            })
            .onErrorResume(e -> {
                log.error("处理失败: {}", e.getMessage());
                return Flux.just(createErrorMessage("处理失败: " + e.getMessage()));
            });
    }

    /**
     * Create an error message component
     */
    private A2uiMessage createErrorMessage(String content) {
        return ComponentMessage.append(
            "main",
            "error_" + System.currentTimeMillis(),
            ComponentSpec.of("Text", Map.of(
                "content", "**错误:** " + content,
                "markdown", true
            ))
        );
    }

    /**
     * Create a text message component
     */
    private A2uiMessage createTextMessage(String content) {
        return ComponentMessage.append(
            "main",
            "text_" + System.currentTimeMillis(),
            ComponentSpec.of("Text", Map.of(
                "content", content,
                "markdown", true
            ))
        );
    }

    /**
     * Internal class to buffer and parse LLM streaming output.
     * Accumulates text chunks and extracts complete JSON objects.
     */
    private class A2uiOutputBuffer {
        private final StringBuilder text = new StringBuilder();
        private final List<A2uiMessage> pending = new ArrayList<>();

        /**
         * Append a chunk of LLM output
         */
        public void append(String chunk) {
            text.append(chunk);
            extractCompleteMessages();
        }

        /**
         * Extract complete JSON objects from the buffer
         */
        private void extractCompleteMessages() {
            String content = text.toString();
            int searchStart = 0;

            while (searchStart < content.length()) {
                // Find next JSON object start
                int jsonStart = content.indexOf('{', searchStart);
                if (jsonStart < 0) {
                    break;
                }

                // Find matching closing brace using depth counting
                int depth = 0;
                int jsonEnd = -1;
                boolean inString = false;
                boolean escaped = false;

                for (int i = jsonStart; i < content.length(); i++) {
                    char c = content.charAt(i);

                    if (escaped) {
                        escaped = false;
                        continue;
                    }

                    if (c == '\\' && inString) {
                        escaped = true;
                        continue;
                    }

                    if (c == '"') {
                        inString = !inString;
                        continue;
                    }

                    if (!inString) {
                        if (c == '{') {
                            depth++;
                        } else if (c == '}') {
                            depth--;
                            if (depth == 0) {
                                jsonEnd = i;
                                break;
                            }
                        }
                    }
                }

                if (jsonEnd > jsonStart) {
                    String jsonCandidate = content.substring(jsonStart, jsonEnd + 1);

                    // Try to parse as A2uiMessage
                    try {
                        // First check if it looks like an A2UI message
                        JsonNode node = objectMapper.readTree(jsonCandidate);
                        if (node.has("type")) {
                            A2uiMessage msg = A2uiEncoder.decode(jsonCandidate, A2uiMessage.class);
                            pending.add(msg);
                            log.debug("Extracted A2uiMessage: type={}", node.get("type").asText());
                        }
                        // Remove processed content
                        text.delete(0, jsonEnd + 1);
                        content = text.toString();
                        searchStart = 0;
                        continue;
                    } catch (Exception e) {
                        // Not valid A2UI JSON, skip this object
                        log.trace("Not an A2UI message: {}", e.getMessage());
                    }
                }

                searchStart = jsonStart + 1;
            }
        }

        /**
         * Get all pending complete messages and clear them
         */
        public List<A2uiMessage> drainMessages() {
            List<A2uiMessage> result = new ArrayList<>(pending);
            pending.clear();
            return result;
        }

        /**
         * Get remaining text that couldn't be parsed as JSON
         */
        public String getRemainingText() {
            return text.toString().trim();
        }
    }
}
