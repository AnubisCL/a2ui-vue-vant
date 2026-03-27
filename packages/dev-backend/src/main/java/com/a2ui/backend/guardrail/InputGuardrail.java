// src/main/java/com/a2ui/backend/guardrail/InputGuardrail.java
package com.a2ui.backend.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Input guardrail for validating and sanitizing user input.
 */
@Slf4j
@Component
public class InputGuardrail {

    private static final int MAX_MESSAGE_LENGTH = 4000;

    // HTML/JS patterns - both validated AND sanitized
    private static final List<Pattern> HTML_JS_PATTERNS = List.of(
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<[^>]*on\\w+\\s*=", Pattern.CASE_INSENSITIVE)
    );

    // Injection patterns - validated only (message is rejected, not sanitized)
    private static final List<String> INJECTION_PATTERNS = List.of(
        "sql注入", "${", "#{"
    );

    /**
     * Validate user message
     */
    public ValidationResult validate(String message) {
        if (message == null || message.isEmpty()) {
            return ValidationResult.failure("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.failure(
                String.format("消息长度不能超过 %d 字符", MAX_MESSAGE_LENGTH));
        }

        String lowerMessage = message.toLowerCase();

        for (String pattern : INJECTION_PATTERNS) {
            if (lowerMessage.contains(pattern.toLowerCase())) {
                log.warn("检测到危险内容: {}", pattern);
                return ValidationResult.failure("消息包含不允许的内容");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Sanitize user message by removing dangerous HTML/JS content.
     * Note: This only handles HTML/JS, not injection patterns.
     * Use validate() for comprehensive security.
     */
    public String sanitize(String message) {
        if (message == null) return null;

        String result = message;
        for (Pattern pattern : HTML_JS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        return result.trim();
    }
}
