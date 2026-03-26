package com.a2ui.backend.llm;

import com.a2ui.backend.config.A2uiProperties;
import com.a2ui.backend.demo.DemoScenario;
import com.a2ui.backend.demo.ScenarioManager;
import com.a2ui.backend.protocol.A2uiEncoder;
import com.a2ui.backend.protocol.builder.A2uiMessageBuilder;
import com.a2ui.backend.protocol.builder.ComponentBuilder;
import com.a2ui.backend.protocol.model.A2uiMessage;
import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import com.a2ui.backend.protocol.model.SurfaceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for streaming A2UI responses from LLM or demo mode.
 *
 * This class provides a unified interface for generating A2UI messages
 * from either the configured LLM or a demo fallback when LLM is not available.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingHandler {

    private final A2uiProperties a2uiProperties;
    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ScenarioManager scenarioManager;
    private final ObjectMapper objectMapper = A2uiEncoder.getMapper();

    // Pattern to match JSON objects in LLM response
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "\\{[^{}]*\"type\"[^{}]*\\}",
        Pattern.DOTALL
    );

    /**
     * Check if LLM is available.
     */
    public boolean isLlmAvailable() {
        return a2uiProperties.getLlm().isConfigured() && chatLanguageModel != null;
    }

    /**
     * Generate A2UI response stream from user message.
     * Uses LLM if available, otherwise falls back to demo mode.
     *
     * @param userMessage The user's input message
     * @param sessionId   The session ID for conversation continuity
     * @return Flux of A2UI messages
     */
    public Flux<A2uiMessage> generateResponse(String userMessage, String sessionId) {
        log.info("Generating response for session: {}, LLM available: {}",
            sessionId, isLlmAvailable());

        // 1. Check demo scenarios first (highest priority)
        Optional<DemoScenario> matchingScenario = scenarioManager.findMatchingScenario(userMessage);
        if (matchingScenario.isPresent()) {
            log.info("Using demo scenario: {}", matchingScenario.get().getName());
            return executeScenario(matchingScenario.get(), userMessage, sessionId);
        }

        // 2. Use LLM if available
        if (isLlmAvailable()) {
            return generateLlmResponse(userMessage, sessionId);
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
                            "content", "Error parsing response",
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
     * Parse JSON string to appropriate A2uiMessage subclass based on type field.
     */
    private A2uiMessage parseA2uiMessage(String json) throws Exception {
        // First, parse as a generic map to get the type
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        String type = (String) map.get("type");

        if (type == null) {
            throw new IllegalArgumentException("Missing 'type' field in message");
        }

        // Deserialize to appropriate subclass based on type
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
     * Generate response using LLM.
     */
    private Flux<A2uiMessage> generateLlmResponse(String userMessage, String sessionId) {
        return Mono.fromCallable(() -> {
                try {
                    log.debug("Calling LLM with message: {}", userMessage);
                    Response<AiMessage> response = chatLanguageModel.generate(
                        dev.langchain4j.data.message.UserMessage.from(userMessage)
                    );
                    return response.content().text();
                } catch (Exception e) {
                    log.error("LLM call failed: {}", e.getMessage(), e);
                    throw new RuntimeException("LLM call failed", e);
                }
            })
            .flatMapMany(responseText -> {
                log.debug("LLM response: {}", responseText);
                return parseAndStreamA2uiMessages(responseText, sessionId);
            })
            .onErrorResume(e -> {
                log.error("Error in LLM response processing: {}", e.getMessage());
                return generateFallbackResponse(userMessage, sessionId);
            });
    }

    /**
     * Parse LLM response text and stream as A2UI messages.
     */
    private Flux<A2uiMessage> parseAndStreamA2uiMessages(String responseText, String sessionId) {
        List<A2uiMessage> messages = new ArrayList<>();
        String surfaceId = "main";

        // Always start with a surface
        messages.add(SurfaceMessage.of(surfaceId, "Chat"));

        // Try to extract JSON objects from the response
        List<String> jsonObjects = extractJsonObjects(responseText);

        if (jsonObjects.isEmpty()) {
            // No JSON found, treat entire response as text
            messages.add(ComponentMessage.append(surfaceId,
                generateId("assistant"),
                ComponentSpec.of("Text", Map.of(
                    "content", responseText,
                    "markdown", true
                ))
            ));
        } else {
            // Parse each JSON object
            for (String json : jsonObjects) {
                try {
                    A2uiMessage message = objectMapper.readValue(json, A2uiMessage.class);
                    if (message != null) {
                        messages.add(message);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse JSON as A2uiMessage: {}", json);
                }
            }

            // Add any non-JSON text before or after JSON objects
            String remainingText = responseText;
            for (String json : jsonObjects) {
                remainingText = remainingText.replace(json, "");
            }
            remainingText = remainingText.trim();
            if (!remainingText.isEmpty()) {
                messages.add(1, ComponentMessage.append(surfaceId,
                    generateId("assistant"),
                    ComponentSpec.of("Text", Map.of(
                        "content", remainingText,
                        "markdown", true
                    ))
                ));
            }
        }

        // Add a divider at the end
        messages.add(ComponentMessage.append(surfaceId,
            generateId("divider"),
            ComponentSpec.of("Divider", Map.of())
        ));

        return Flux.fromIterable(messages)
            .delayElements(Duration.ofMillis(a2uiProperties.getDemo().getStreamingDelayMs()));
    }

    /**
     * Extract JSON objects from text that contain "type" field.
     */
    private List<String> extractJsonObjects(String text) {
        List<String> jsonObjects = new ArrayList<>();

        // Simple extraction - find JSON objects with "type" field
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

        // 1. Create surface
        messages.add(SurfaceMessage.of(surfaceId, "Chat"));

        // 2. Add user message as text
        messages.add(ComponentMessage.append(surfaceId,
            generateId("user"),
            ComponentSpec.of("Text", Map.of(
                "content", "**You:** " + userMessage,
                "markdown", true
            ))
        ));

        // 3. Add assistant response
        String responseContent = generateDemoTextResponse(userMessage);
        messages.add(ComponentMessage.append(surfaceId,
            generateId("assistant"),
            ComponentSpec.of("Text", Map.of(
                "content", responseContent,
                "markdown", true
            ))
        ));

        // 4. Add demo components based on message content
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

        // 5. Add divider
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
                "content", "**Error:** Failed to generate LLM response. Using demo mode.\n\n" +
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
        response.append("**A2UI Assistant:**\n\n");

        if (containsKeywords(userMessage, "hello", "hi", "hey")) {
            response.append("Hello! Welcome to A2UI. How can I help you today?\n\n");
            response.append("Try asking for:\n");
            response.append("- A **chart** or **data** visualization\n");
            response.append("- A **form** for input\n");
            response.append("- A **table** or **list** of items\n");
        } else if (containsKeywords(userMessage, "chart", "graph", "data")) {
            response.append("Here's a sample chart visualization for you!\n\n");
            response.append("The chart shows weekly data trends. You can customize this with real data.");
        } else if (containsKeywords(userMessage, "form", "input", "register")) {
            response.append("I've created a demo form for you below.\n\n");
            response.append("Fill out the fields and submit to see the form in action.");
        } else if (containsKeywords(userMessage, "table", "list")) {
            response.append("Here's a sample data table.\n\n");
            response.append("Tables are great for displaying structured data.");
        } else if (containsKeywords(userMessage, "help", "what", "how")) {
            response.append("A2UI is a dynamic UI protocol that renders components in real-time.\n\n");
            response.append("**Available features:**\n");
            response.append("- Real-time streaming via SSE or WebSocket\n");
            response.append("- Dynamic component rendering\n");
            response.append("- Charts, forms, tables, and more\n\n");
            response.append("Try typing keywords like 'chart', 'form', or 'table'!");
        } else {
            response.append("I received your message: \"").append(userMessage).append("\"\n\n");
            response.append("This is a demo response. Configure an LLM provider for intelligent responses.\n\n");
            response.append("*Tip: Try asking for a chart, form, or table to see dynamic components!*");
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
                "required", true,
                "options", Map.of("placeholder", "Enter your name")
            ),
            Map.of(
                "name", "email",
                "label", "Email Address",
                "type", "text",
                "required", true,
                "options", Map.of("placeholder", "Enter your email")
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
            ),
            Map.of(
                "name", "message",
                "label", "Message",
                "type", "textarea",
                "required", true,
                "options", Map.of("placeholder", "Enter your message", "rows", 4)
            )
        );

        return ComponentMessage.append(surfaceId,
            generateId("form"),
            ComponentSpec.of("Form", Map.of(
                "fields", fields,
                "submitLabel", "Submit Form",
                "action", "/api/form/submit"
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
            Map.of("key", "status", "title", "Status", "width", 100),
            Map.of("key", "date", "title", "Date", "width", 120)
        );

        List<Map<String, Object>> data = List.of(
            Map.of("id", 1, "name", "Task Alpha", "status", "Active", "date", "2024-01-15"),
            Map.of("id", 2, "name", "Task Beta", "status", "Pending", "date", "2024-01-16"),
            Map.of("id", 3, "name", "Task Gamma", "status", "Completed", "date", "2024-01-17"),
            Map.of("id", 4, "name", "Task Delta", "status", "Active", "date", "2024-01-18")
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
