package com.a2ui.backend.guardrail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResult {
    private boolean success;
    private String message;

    public static ValidationResult success() {
        return ValidationResult.builder().success(true).build();
    }

    public static ValidationResult failure(String message) {
        return ValidationResult.builder().success(false).message(message).build();
    }
}
