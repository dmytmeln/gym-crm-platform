package com.gym.crm.core.exception;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public <T> ValidationException(Set<ConstraintViolation<T>> violations) {
        super(formatViolations(violations));
    }

    private static <T> String formatViolations(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
    }

}
