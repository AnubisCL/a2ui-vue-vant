// src/main/java/com/a2ui/backend/guardrail/OutputGuardrail.java
package com.a2ui.backend.guardrail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Output guardrail for validating A2UI JSON responses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutputGuardrail {

    private static final Set<String> ALLOWED_COMPONENT_TYPES = Set.of(
        "Text", "Chart", "Form", "Card", "Button", "Divider",
        "Image", "Table", "List", "Tabs", "Spacer"
    );

    private final ObjectMapper objectMapper;

    /**
     * Validate A2UI JSON output
     * @param json A2UI JSON string
     * @return ValidationResult
     */
    public ValidationResult validate(String json) {
        if (json == null || json.isEmpty()) {
            return ValidationResult.failure("输出不能为空");
        }

        try {
            JsonNode node = objectMapper.readTree(json);

            // Check required fields
            if (!node.has("type")) {
                return ValidationResult.failure("JSON 缺少 type 字段");
            }

            if (!node.has("surfaceId")) {
                return ValidationResult.failure("JSON 缺少 surfaceId 字段");
            }

            // Check component type whitelist
            if (node.has("component") && node.get("component").has("type")) {
                String componentType = node.get("component").get("type").asText();
                if (!ALLOWED_COMPONENT_TYPES.contains(componentType)) {
                    log.warn("不允许的组件类型: {}", componentType);
                    return ValidationResult.failure("不支持的组件类型: " + componentType);
                }
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            return ValidationResult.failure("JSON 格式错误: " + e.getMessage());
        }
    }
}
