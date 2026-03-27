package com.a2ui.backend.llm;

import com.a2ui.backend.agent.coordinator.AgentCoordinator;
import com.a2ui.backend.config.A2uiProperties;
import com.a2ui.backend.demo.DemoScenario;
import com.a2ui.backend.demo.ScenarioManager;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.SurfaceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for streaming A2UI responses from LLM or demo mode.
 *
 * This class provides a unified interface for generating A2UI messages
 * from either the configured LLM or a demo fallback when LLM is not available.
 *
 * Supports:
 * - Demo scenarios (predefined responses for common queries)
 * - LLM-powered responses using ChatLanguageModel
 * - A2uiAssistant for streaming responses with memory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingHandler {

    private final A2uiProperties a2uiProperties;
    private final ScenarioManager scenarioManager;
    private final ObjectMapper objectMapper = A2uiEncoder.getMapper();

    // Optional bean - may not be present if LLM is not configured
    @Autowired(required = false)
    private A2uiAssistant a2uiAssistant;

    // Optional AgentCoordinator (preferred over A2uiAssistant)
    @Autowired(required = false)
    private AgentCoordinator agentCoordinator;

    /**
     * Check if LLM is available (assistant bean is present).
     */
    public boolean isLlmAvailable() {
        return a2uiAssistant != null && a2uiProperties.getLlm().isConfigured();
    }

    /**
     * Generate A2UI response stream from user message.
     * Priority:
     * 1. Demo scenarios (if LLM not configured or bypassDemo is false)
     * 2. LLM with A2uiAssistant (if configured and bypassDemo is true or no demo match)
     * 3. Generic demo response
     *
     * @param userMessage The user's input message
     * @param sessionId   The session ID for conversation continuity
     * @return Flux of A2UI messages
     */
    public Flux<A2uiMessage> generateResponse(String userMessage, String sessionId) {
        log.info("Generating response for session: {}, Assistant available: {}",
            sessionId, a2uiAssistant != null);

        // Check if we should bypass demo scenarios
        boolean shouldBypassDemo = !a2uiProperties.getDemo().isBypassDemo()
            && a2uiAssistant != null;

        // 1. Check demo scenarios first (unless bypassDemo is enabled)
        if (!shouldBypassDemo) {
            Optional<DemoScenario> matchingScenario = scenarioManager.findMatchingScenario(userMessage);
            if (matchingScenario.isPresent()) {
                log.info("Using demo scenario: {}", matchingScenario.get().getName());
                return executeScenario(matchingScenario.get(), userMessage, sessionId);
            }
        } else {
            log.info("Demo scenarios bypassed - using LLM");
        }

        // 2. Use AgentCoordinator (preferred) or A2uiAssistant (legacy)
        if (agentCoordinator != null) {
            log.info("Using AgentCoordinator for session: {}", sessionId);
            return agentCoordinator.processMessage(userMessage, sessionId);
        }

        if (a2uiAssistant != null) {
            return generateAssistantResponse(userMessage, sessionId);
        }

        // 3. Fall back to generic demo response
        return generateDemoResponse(userMessage, sessionId);
    }

    /**
     * Execute a demo scenario and convert its output to A2uiMessages.
     */
    private Flux<A2uiMessage> executeScenario(DemoScenario scenario, String userMessage, String sessionId) {
        return scenario.execute(userMessage, sessionId)
            .map(json -> {
                try {
                    return parseA2uiMessage(json);
                } catch (Exception e) {
                    log.warn("Failed to parse scenario output as A2uiMessage: {}, error: {}", json, e.getMessage());
                    // Return a text component with the raw JSON as fallback
                    return (A2uiMessage) ComponentMessage.append("main",
                        generateId("error"),
                        ComponentSpec.of("Text", Map.of(
                            "content", "Error parsing response: " + e.getMessage(),
                            "markdown", true
                        ))
                    );
                }
            })
            .onErrorResume(e -> {
                log.error("Error executing scenario: {}", e.getMessage());
                return generateFallbackResponse(userMessage, sessionId);
            });
    }

    /**
     * Generate response using A2uiAssistant (supports streaming and memory).
     */
    private Flux<A2uiMessage> generateAssistantResponse(String userMessage, String sessionId) {
        Long memoryId = sessionId.hashCode() & 0xFFFFFFFFL;

        log.info("Using A2uiAssistant for session: {}, memoryId: {}", sessionId, memoryId);

        try {
            return a2uiAssistant.chatStream(memoryId, userMessage)
                .transform(this::parseAssistantStream)
                .onErrorResume(e -> {
                    log.error("A2uiAssistant error: {}", e.getMessage());
                    return generateFallbackResponse(userMessage, sessionId);
                });
        } catch (Exception e) {
            log.error("Failed to call A2uiAssistant: {}", e.getMessage());
            return generateFallbackResponse(userMessage, sessionId);
        }
    }

    /**
     * Parse assistant stream output to A2UI messages.
     * Uses a buffer to accumulate tokens until complete JSON objects are found.
     */
    private Flux<A2uiMessage> parseAssistantStream(Flux<String> stream) {
        // Use scan to maintain buffer state across stream chunks
        return stream
            .scan(new A2uiOutputBuffer(), A2uiOutputBuffer::append)
            .flatMap(buffer -> {
                List<A2uiMessage> messages = buffer.drainPending();
                if (messages.isEmpty()) {
                    return Flux.empty();
                }
                // Emit each message with a small delay for streaming effect
                return Flux.fromIterable(messages)
                    .delayElements(Duration.ofMillis(30));
            })
            .switchIfEmpty(Mono.just((A2uiMessage) ComponentMessage.append("main",
                generateId("assistant"),
                ComponentSpec.of("Text", Map.of(
                    "content", "No response generated",
                    "markdown", true
                ))
            )));
    }

    /**
     * Output buffer that accumulates LLM tokens until complete JSON objects are found.
     * Solves the token fragmentation problem where each character was sent as a separate component.
     */
    private static class A2uiOutputBuffer {
        private final StringBuilder accumulated = new StringBuilder();
        private final List<A2uiMessage> pendingMessages = new ArrayList<>();

        /**
         * Append a chunk of text and extract any complete JSON objects.
         */
        public A2uiOutputBuffer append(String chunk) {
            accumulated.append(chunk);
            extractCompleteMessages();
            return this;
        }

        /**
         * Extract complete JSON objects from accumulated text using bracket matching.
         * Complete JSON objects are parsed and added to pendingMessages.
         * Remaining text is kept in the buffer for further accumulation.
         */
        private void extractCompleteMessages() {
            String text = accumulated.toString();
            int n = text.length();

            for (int i = 0; i < n; i++) {
                char c = text.charAt(i);

                // Skip non-JSON content at the beginning
                if (c == '{' && i == findJsonStart(text, i)) {
                    // Found start of a potential JSON object
                    Integer endIndex = findMatchingBrace(text, i);
                    if (endIndex != null) {
                        String json = text.substring(i, endIndex + 1);
                        try {
                            // Only parse if it looks like an A2UI message (has "type" field)
                            if (json.contains("\"type\"")) {
                                A2uiMessage message = parseA2uiMessageStatic(json);
                                if (message != null) {
                                    pendingMessages.add(message);
                                }
                            }
                        } catch (Exception e) {
                            // JSON parsing failed, might be incomplete, continue accumulating
                            log.debug("Buffer: incomplete JSON, continuing accumulation");
                        }
                        // Remove processed portion including any trailing content
                        accumulated.delete(0, endIndex + 1);
                        i = -1; // Reset to start
                        n = accumulated.length();
                    } else {
                        // No matching brace found yet, wait for more content
                        break;
                    }
                }
            }
        }

        /**
         * Find the start of a JSON object, skipping whitespace and newlines.
         */
        private int findJsonStart(String text, int from) {
            while (from < text.length()) {
                char c = text.charAt(from);
                if (c == '{') return from;
                if (!Character.isWhitespace(c)) break;
                from++;
            }
            return from;
        }

        /**
         * Find the matching closing brace for the opening brace at startIndex.
         * Returns the index of the matching brace, or null if not found.
         */
        private Integer findMatchingBrace(String text, int startIndex) {
            int depth = 0;
            boolean inString = false;
            char prevChar = 0;

            for (int i = startIndex; i < text.length(); i++) {
                char c = text.charAt(i);

                if (c == '"' && prevChar != '\\') {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            return i;
                        }
                    }
                }

                prevChar = c;
            }
            return null; // Not complete yet
        }

        /**
         * Drain all pending messages and return them.
         */
        public List<A2uiMessage> drainPending() {
            List<A2uiMessage> result = new ArrayList<>(pendingMessages);
            pendingMessages.clear();
            return result;
        }

        /**
         * Get any remaining text that hasn't been parsed into a complete JSON object.
         */
        public String getRemainingText() {
            return accumulated.toString();
        }

        /**
         * Parse a JSON string to an A2uiMessage (static version for use in buffer).
         */
        private static A2uiMessage parseA2uiMessageStatic(String json) {
            try {
                ObjectMapper mapper = A2uiEncoder.getMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> map = mapper.readValue(json, Map.class);
                String type = (String) map.get("type");

                if (type == null) {
                    return null;
                }

                switch (type) {
                    case "surface":
                        return mapper.readValue(json, SurfaceMessage.class);
                    case "component":
                        return mapper.readValue(json, ComponentMessage.class);
                    case "dataModel":
                        return mapper.readValue(json, com.a2ui.backend.protocol.model.DataModelMessage.class);
                    case "deleteSurface":
                        return mapper.readValue(json, com.a2ui.backend.protocol.model.DeleteSurfaceMessage.class);
                    default:
                        return null;
                }
            } catch (Exception e) {
                log.debug("Failed to parse JSON: {}", e.getMessage());
                return null;
            }
        }
    }

    /**
     * Extract A2UI messages from assistant output text.
     * This method is kept for backward compatibility but is superseded by A2uiOutputBuffer.
     */
    private List<A2uiMessage> extractA2uiMessages(String text) {
        List<A2uiMessage> messages = new ArrayList<>();

        // Extract JSON objects from text
        List<String> jsonObjects = extractJsonObjects(text);

        for (String json : jsonObjects) {
            try {
                A2uiMessage message = parseA2uiMessage(json);
                messages.add(message);
            } catch (Exception e) {
                log.warn("Failed to parse JSON as A2uiMessage: {}", e.getMessage());
            }
        }

        // If no JSON found, create a text component
        if (messages.isEmpty() && !text.trim().isEmpty()) {
            String cleanText = text.replaceAll("```json\\s*", "")
                                   .replaceAll("```\\s*", "")
                                   .trim();
            if (!cleanText.isEmpty()) {
                messages.add(ComponentMessage.append("main",
                    generateId("assistant"),
                    ComponentSpec.of("Text", Map.of(
                        "content", cleanText,
                        "markdown", true
                    ))
                ));
            }
        }

        return messages;
    }

    /**
     * Parse JSON string to appropriate A2uiMessage subclass based on type field.
     */
    private A2uiMessage parseA2uiMessage(String json) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        String type = (String) map.get("type");

        if (type == null) {
            throw new IllegalArgumentException("Missing 'type' field in message");
        }

        switch (type) {
            case "surface":
                return objectMapper.readValue(json, SurfaceMessage.class);
            case "component":
                return objectMapper.readValue(json, ComponentMessage.class);
            case "dataModel":
                return objectMapper.readValue(json, com.a2ui.backend.protocol.model.DataModelMessage.class);
            case "deleteSurface":
                return objectMapper.readValue(json, com.a2ui.backend.protocol.model.DeleteSurfaceMessage.class);
            default:
                throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }

    /**
     * Extract JSON objects from text that contain "type" field.
     */
    private List<String> extractJsonObjects(String text) {
        List<String> jsonObjects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    String json = text.substring(start, i + 1);
                    if (json.contains("\"type\"")) {
                        jsonObjects.add(json);
                    }
                    start = -1;
                }
            }
        }

        return jsonObjects;
    }

    /**
     * Generate demo response when LLM is not available.
     */
    private Flux<A2uiMessage> generateDemoResponse(String userMessage, String sessionId) {
        String surfaceId = "main";
        List<A2uiMessage> messages = new ArrayList<>();

        messages.add(SurfaceMessage.of(surfaceId, "Chat"));

        messages.add(ComponentMessage.append(surfaceId,
            generateId("user"),
            ComponentSpec.of("Text", Map.of(
                "content", "**You:** " + userMessage,
                "markdown", true
            ))
        ));

        String responseContent = generateDemoTextResponse(userMessage);
        messages.add(ComponentMessage.append(surfaceId,
            generateId("assistant"),
            ComponentSpec.of("Text", Map.of(
                "content", responseContent,
                "markdown", true
            ))
        ));

        A2uiProperties.DemoConfig demoConfig = a2uiProperties.getDemo();

        if (demoConfig.isEnableCharts() && containsKeywords(userMessage, "chart", "graph", "data", "visualization")) {
            messages.add(createDemoChart(surfaceId));
        }

        if (demoConfig.isEnableForms() && containsKeywords(userMessage, "form", "input", "submit", "register")) {
            messages.add(createDemoForm(surfaceId));
        }

        if (demoConfig.isEnableTables() && containsKeywords(userMessage, "table", "list", "grid")) {
            messages.add(createDemoTable(surfaceId));
        }

        messages.add(ComponentMessage.append(surfaceId,
            generateId("divider"),
            ComponentSpec.of("Divider", Map.of())
        ));

        return Flux.fromIterable(messages)
            .delayElements(Duration.ofMillis(demoConfig.getStreamingDelayMs()));
    }

    /**
     * Generate fallback response on error.
     */
    private Flux<A2uiMessage> generateFallbackResponse(String userMessage, String sessionId) {
        String surfaceId = "main";
        List<A2uiMessage> messages = new ArrayList<>();

        messages.add(SurfaceMessage.of(surfaceId, "Chat"));

        messages.add(ComponentMessage.append(surfaceId,
            generateId("error"),
            ComponentSpec.of("Text", Map.of(
                "content", "**Error:** Failed to generate LLM response. Please check your API key and configuration.\n\n" +
                    "Your message was: \"" + userMessage + "\"",
                "markdown", true
            ))
        ));

        messages.add(ComponentMessage.append(surfaceId,
            generateId("divider"),
            ComponentSpec.of("Divider", Map.of())
        ));

        return Flux.fromIterable(messages);
    }

    /**
     * Generates a demo text response based on user message content.
     */
    private String generateDemoTextResponse(String userMessage) {
        StringBuilder response = new StringBuilder();
        response.append("**A2UI Assistant (Demo Mode):**\n\n");

        if (containsKeywords(userMessage, "hello", "hi", "hey")) {
            response.append("Hello! Welcome to A2UI Demo. How can I help you today?\n\n");
            response.append("Try asking for:\n");
            response.append("- A **chart** or **data** visualization\n");
            response.append("- A **form** for input\n");
            response.append("- A **table** or **list** of items\n");
            response.append("- **Query orders** to see order management\n");
            response.append("- **Statistics** or **chart** for data visualization\n");
        } else if (containsKeywords(userMessage, "help", "what", "how", "功能")) {
            response.append("A2UI is a dynamic UI protocol that renders components in real-time.\n\n");
            response.append("**Available features:**\n");
            response.append("- Real-time streaming via SSE or WebSocket\n");
            response.append("- Dynamic component rendering\n");
            response.append("- Charts, forms, tables, and more\n\n");
            response.append("**Demo scenarios:**\n");
            response.append("- Query orders: See order list and details\n");
            response.append("- Statistics/Chart: View sales data charts\n");
            response.append("- Data statistics: Interactive query forms\n\n");
            response.append("Configure an LLM provider for intelligent responses!");
        } else {
            response.append("I received your message: \"").append(userMessage).append("\"\n\n");
            response.append("This is a demo response. Configure an LLM provider for intelligent responses.\n\n");
            response.append("*Tip: Try asking for 'chart', 'form', 'orders', or 'statistics'!*");
        }

        return response.toString();
    }

    /**
     * Creates a demo chart component.
     */
    private ComponentMessage createDemoChart(String surfaceId) {
        Map<String, Object> chartOption = Map.of(
            "title", Map.of("text", "Weekly Performance"),
            "tooltip", Map.of("trigger", "axis"),
            "legend", Map.of("data", List.of("Sales", "Revenue")),
            "xAxis", Map.of(
                "type", "category",
                "data", List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            ),
            "yAxis", Map.of("type", "value"),
            "series", List.of(
                Map.of(
                    "name", "Sales",
                    "data", List.of(120, 200, 150, 80, 70, 110, 130),
                    "type", "line",
                    "smooth", true
                ),
                Map.of(
                    "name", "Revenue",
                    "data", List.of(90, 150, 130, 60, 50, 90, 110),
                    "type", "bar"
                )
            )
        );

        return ComponentMessage.append(surfaceId,
            generateId("chart"),
            ComponentSpec.of("Chart", Map.of("option", chartOption, "height", 300))
        );
    }

    /**
     * Creates a demo form component.
     */
    private ComponentMessage createDemoForm(String surfaceId) {
        List<Map<String, Object>> fields = List.of(
            Map.of(
                "name", "name",
                "label", "Full Name",
                "type", "text",
                "required", true
            ),
            Map.of(
                "name", "category",
                "label", "Category",
                "type", "select",
                "required", false,
                "options", Map.of("options", List.of(
                    Map.of("value", "general", "label", "General Inquiry"),
                    Map.of("value", "support", "label", "Technical Support"),
                    Map.of("value", "feedback", "label", "Feedback")
                ))
            )
        );

        return ComponentMessage.append(surfaceId,
            generateId("form"),
            ComponentSpec.of("Form", Map.of(
                "fields", fields,
                "submitLabel", "Submit",
                "action", "demo-form-submit"
            ))
        );
    }

    /**
     * Creates a demo table component.
     */
    private ComponentMessage createDemoTable(String surfaceId) {
        List<Map<String, Object>> columns = List.of(
            Map.of("key", "id", "title", "ID", "width", 80),
            Map.of("key", "name", "title", "Name", "width", 150),
            Map.of("key", "status", "title", "Status", "width", 100)
        );

        List<Map<String, Object>> data = List.of(
            Map.of("id", 1, "name", "Task Alpha", "status", "Active"),
            Map.of("id", 2, "name", "Task Beta", "status", "Pending"),
            Map.of("id", 3, "name", "Task Gamma", "status", "Completed")
        );

        return ComponentMessage.append(surfaceId,
            generateId("table"),
            ComponentSpec.of("Table", Map.of(
                "columns", columns,
                "dataSource", data,
                "pagination", Map.of("pageSize", 10)
            ))
        );
    }

    /**
     * Checks if the message contains any of the given keywords (case-insensitive).
     */
    private boolean containsKeywords(String message, String... keywords) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : keywords) {
            if (lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a unique component ID with prefix.
     */
    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
