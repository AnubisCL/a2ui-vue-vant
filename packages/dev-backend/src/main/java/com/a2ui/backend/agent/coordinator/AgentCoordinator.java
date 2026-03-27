package com.a2ui.backend.agent.coordinator;

import com.a2ui.backend.agent.general.GeneralAgent;
import com.a2ui.backend.guardrail.InputGuardrail;
import com.a2ui.backend.guardrail.OutputGuardrail;
import com.a2ui.backend.guardrail.ValidationResult;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

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

    /**
     * Process user message through the agent pipeline.
     *
     * Pipeline:
     * 1. Input validation
     * 2. Input sanitization
     * 3. GeneralAgent.chat() - LLM processing
     * 4. Output validation
     * 5. Parse to A2uiMessage
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

        // 4. Call GeneralAgent
        return generalAgent.chat(sanitizedMessage, memoryId)
            .map(json -> {
                // 5. Output validation
                ValidationResult outputResult = outputGuardrail.validate(json);
                if (!outputResult.isSuccess()) {
                    log.warn("输出校验失败: {}", outputResult.getMessage());
                    return createErrorMessage("输出格式错误");
                }

                // 6. Parse to A2uiMessage
                try {
                    return A2uiEncoder.decode(json, A2uiMessage.class);
                } catch (Exception e) {
                    log.error("解析 A2UI 消息失败: {}", e.getMessage());
                    return createErrorMessage("解析响应失败");
                }
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
}
