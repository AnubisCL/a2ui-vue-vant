package com.a2ui.backend.llm;

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

    /**
     * Check if LLM is available (assistant bean is present).
     */
    public boolean isLlmAvailable() {
        return a2uiAssistant != null && a2uiProperties.getLlm().isConfigured();
    }

    /**
     * Generate A2UI response stream from user message.
     * Priority:
     * 1. Demo scenarios (highest priority for common queries)
     * 2. LLM with A2uiAssistant (if configured)
     * 3. Generic demo response
     *
     * @param userMessage The user's input message
     * @param sessionId   The session ID for conversation continuity
     * @return Flux of A2UI messages
     */
    public Flux<A2uiMessage> generateResponse(String userMessage, String sessionId) {
        log.info("Generating response for session: {}, Assistant available: {}",
            sessionId, a2uiAssistant != null);

        // 1. Check demo scenarios first (highest priority)
        Optional<DemoScenario> matchingScenario = scenarioManager.findMatchingScenario(userMessage);
        if (matchingScenario.isPresent()) {
            log.info("Using demo scenario: {}", matchingScenario.get().getName());
            return executeScenario(matchingScenario.get(), userMessage, sessionId);
        }

        // 2. Use LLM with A2uiAssistant if available
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
     */
    private Flux<A2uiMessage> parseAssistantStream(Flux<String> stream) {
        return stream
            .flatMap(text -> Flux.fromIterable(extractA2uiMessages(text)))
            .switchIfEmpty(Mono.just((A2uiMessage) ComponentMessage.append("main",
                generateId("assistant"),
                ComponentSpec.of("Text", Map.of(
                    "content", "No response generated",
                    "markdown", true
                ))
            )))
            .delayElements(Duration.ofMillis(50)); // Small delay for streaming effect
    }

    /**
     * Extract A2UI messages from assistant output text.
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
