package com.a2ui.backend.guardrail;

import lombok.Getter;

@Getter
public final class ValidationResult {
    private final boolean success;
    private final String message;

    private ValidationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
            "success=" + success +
            ", message='" + message + '\'' +
            '}';
    }
}
