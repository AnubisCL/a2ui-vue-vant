package com.a2ui.backend.tools;

import com.a2ui.backend.protocol.model.ComponentMessage;
import com.a2ui.backend.protocol.model.ComponentSpec;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool for generating form components to collect user input.
 *
 * When the LLM determines that it needs more information from the user,
 * it calls this tool to generate a form with the appropriate fields.
 * The form is cached via ComponentResultCache for direct output.
 */
@Slf4j
@Component
public class FormGeneratorTool {

    private final ComponentResultCache componentResultCache;

    public FormGeneratorTool(ComponentResultCache componentResultCache) {
        this.componentResultCache = componentResultCache;
    }

    /**
     * Generate a form to collect user input when more information is needed.
     *
     * @param purpose     Description of what the form is for
     * @param fieldsJson  JSON array of field definitions.
     *                    Each field: {"name":"...", "label":"...", "type":"text|select|textarea|date|number", "required":true/false,
     *                    "options":{"options":[{"value":"...","label":"..."}]}}
     * @param submitLabel Label for the submit button
     * @param action      Action identifier for form submission handling
     * @param memoryId    Session memory ID (injected by LangChain4j)
     * @return JSON string describing the generated form
     */
    @Tool("Generate a form to collect user input when more information is needed. " +
          "Use this when the user's request is ambiguous or requires additional parameters. " +
          "The form will be displayed to the user for filling.")
    public String generateForm(
        @P("Brief description of what the form collects, e.g. 'Sales query parameters'") String purpose,
        @P("JSON array of fields. Each field: {\"name\":\"fieldName\",\"label\":\"Display Label\"," +
           "\"type\":\"text|select|textarea|date|number\",\"required\":true/false," +
           "\"options\":{\"options\":[{\"value\":\"v\",\"label\":\"L\"}]}}") String fieldsJson,
        @P("Submit button label, e.g. '查询', '提交'") String submitLabel,
        @P("Action identifier for this form, e.g. 'sales-query', 'data-filter'") String action,
        @ToolMemoryId Long memoryId
    ) {
        log.info("Generating form: purpose={}, action={}, memoryId={}", purpose, action, memoryId);

        List<Map<String, Object>> fields = parseFields(fieldsJson);

        String componentId = "form_" + System.currentTimeMillis();

        ComponentMessage formMessage = ComponentMessage.append(
            "main",
            componentId,
            ComponentSpec.of("Form", Map.of(
                "fields", fields,
                "submitLabel", submitLabel != null ? submitLabel : "Submit",
                "action", action != null ? action : "form-submit"
            ))
        );

        componentResultCache.addComponent(memoryId, formMessage);
        log.info("Form component cached: componentId={}, fields={}, memoryId={}", componentId, fields.size(), memoryId);

        // Return description for LLM context
        Map<String, Object> result = Map.of(
            "generated", true,
            "componentId", componentId,
            "fieldCount", fields.size(),
            "action", action != null ? action : "form-submit"
        );
        return formatAsJson(result);
    }

    /**
     * Parse fields JSON string into a list of field maps.
     * Handles the format: [{"name":"...","label":"...","type":"...","required":true/false,...},...]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseFields(String fieldsJson) {
        List<Map<String, Object>> fields = new ArrayList<>();

        if (fieldsJson == null || fieldsJson.isBlank()) {
            // Default: a single text field
            fields.add(Map.of(
                "name", "input",
                "label", "Input",
                "type", "text",
                "required", true
            ));
            return fields;
        }

        try {
            String clean = fieldsJson.trim();
            if (clean.startsWith("[")) {
                // Array format - parse individual objects
                // Simple bracket-matching parser for each { } in the array
                List<String> objects = splitJsonObjects(clean);
                for (String obj : objects) {
                    Map<String, Object> field = parseJsonObject(obj);
                    if (field != null && field.containsKey("name")) {
                        fields.add(field);
                    }
                }
            } else if (clean.startsWith("{")) {
                // Single object
                Map<String, Object> field = parseJsonObject(clean);
                if (field != null && field.containsKey("name")) {
                    fields.add(field);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse fields JSON: {}, using defaults", e.getMessage());
            fields.add(Map.of(
                "name", "input",
                "label", "Input",
                "type", "text",
                "required", true
            ));
        }

        if (fields.isEmpty()) {
            fields.add(Map.of(
                "name", "input",
                "label", "Input",
                "type", "text",
                "required", true
            ));
        }

        return fields;
    }

    /**
     * Split a JSON array string into individual JSON object strings
     */
    private List<String> splitJsonObjects(String jsonArray) {
        List<String> objects = new ArrayList<>();
        String content = jsonArray;
        // Remove surrounding brackets
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

        int depth = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    /**
     * Simple JSON object parser for field definitions.
     * Handles string, boolean, and nested object values.
     */
    private Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || !json.startsWith("{")) return result;

        String content = json.substring(1, json.length() - 1).trim();
        // Split by top-level commas (respecting nested objects)
        List<String> pairs = splitKeyValuePairs(content);

        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx < 0) continue;

            String key = pair.substring(0, colonIdx).trim().replace("\"", "");
            String valueStr = pair.substring(colonIdx + 1).trim();

            Object value = parseValue(valueStr);
            result.put(key, value);
        }
        return result;
    }

    private List<String> splitKeyValuePairs(String content) {
        List<String> pairs = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inString = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    pairs.add(content.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        if (start < content.length()) {
            pairs.add(content.substring(start).trim());
        }
        return pairs;
    }

    private Object parseValue(String valueStr) {
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        if ("true".equalsIgnoreCase(valueStr)) return true;
        if ("false".equalsIgnoreCase(valueStr)) return false;
        if ("null".equalsIgnoreCase(valueStr)) return null;
        if (valueStr.startsWith("{")) {
            // Nested object (e.g., options) - store as parsed map
            return parseJsonObject(valueStr);
        }
        if (valueStr.startsWith("[")) {
            // Array - parse as List of maps/objects
            return parseJsonArray(valueStr);
        }
        // Try number
        try {
            if (valueStr.contains(".")) return Double.parseDouble(valueStr);
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return valueStr;
        }
    }

    /**
     * Parse a JSON array string into a List of objects.
     * Handles arrays of objects: [{"key":"value"},...] and arrays of strings.
     */
    private List<Object> parseJsonArray(String jsonArray) {
        List<Object> result = new ArrayList<>();
        if (jsonArray == null || !jsonArray.startsWith("[")) return result;

        String content = jsonArray.substring(1).trim();
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1).trim();
        if (content.isEmpty()) return result;

        // Split by top-level commas
        List<String> items = splitTopLevel(content, ',');
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("{")) {
                result.add(parseJsonObject(item));
            } else if (item.startsWith("[")) {
                result.add(parseJsonArray(item));
            } else if (item.startsWith("\"") && item.endsWith("\"")) {
                result.add(item.substring(1, item.length() - 1));
            } else if ("true".equalsIgnoreCase(item)) {
                result.add(true);
            } else if ("false".equalsIgnoreCase(item)) {
                result.add(false);
            } else {
                try {
                    result.add(Integer.parseInt(item));
                } catch (NumberFormatException e) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * Split a string by a delimiter, respecting nested brackets and strings.
     */
    private List<String> splitTopLevel(String content, char delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean inString = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == delimiter && depth == 0) {
                    parts.add(content.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        if (start < content.length()) {
            parts.add(content.substring(start).trim());
        }
        return parts;
    }

    private String formatAsJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(", ");
            first = false;
            json.append("\"").append(entry.getKey()).append("\": ");
            json.append(valueToJson(entry.getValue()));
        }
        json.append("}");
        return json.toString();
    }

    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append("\"").append(entry.getKey()).append("\": ");
                sb.append(valueToJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
