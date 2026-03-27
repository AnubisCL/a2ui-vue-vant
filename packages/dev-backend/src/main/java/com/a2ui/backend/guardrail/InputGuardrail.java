package com.a2ui.backend.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Input guardrail for validating and sanitizing user input.
 */
@Slf4j
@Component
public class InputGuardrail {

    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final List<String> BLOCKED_PATTERNS = List.of(
        "<script", "javascript:", "onerror=", "onclick=",
        "sql注入", "';--", "${", "#{"  // 注入检测
    );

    /**
     * Validate user message
     * @param message User input
     * @return ValidationResult
     */
    public ValidationResult validate(String message) {
        if (message == null || message.isEmpty()) {
            return ValidationResult.failure("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.failure(
                String.format("消息长度不能超过 %d 字符", MAX_MESSAGE_LENGTH));
        }
        for (String pattern : BLOCKED_PATTERNS) {
            if (message.toLowerCase().contains(pattern.toLowerCase())) {
                log.warn("检测到危险内容: {}", pattern);
                return ValidationResult.failure("消息包含不允许的内容");
            }
        }
        return ValidationResult.success();
    }

    /**
     * Sanitize user message by removing dangerous content
     * @param message User input
     * @return Sanitized message
     */
    public String sanitize(String message) {
        if (message == null) return null;
        return message
            .replaceAll("<script[^>]*>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+=", "")
            .trim();
    }
}
