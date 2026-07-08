package com.gym.crm.workload.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ValidationUtils {

    public static final String DEFAULT_DELIMITER = ", ";

    private static final String VIOLATION_MESSAGE_FORMAT = "%s: %s";

    public static String formatViolations(Collection<? extends ConstraintViolation<?>> violations, String delimiter) {
        return violations.stream()
                .filter(violation -> violation.getPropertyPath() != null && violation.getMessage() != null)
                .sorted(comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> VIOLATION_MESSAGE_FORMAT.formatted(violation.getPropertyPath(), violation.getMessage()))
                .collect(joining(delimiter));
    }

    public static String formatViolations(ConstraintViolationException exception) {
        return formatViolations(exception.getConstraintViolations(), DEFAULT_DELIMITER);
    }

    public static String formatViolations(ConstraintViolationException exception, String delimiter) {
        return formatViolations(exception.getConstraintViolations(), delimiter);
    }

}
