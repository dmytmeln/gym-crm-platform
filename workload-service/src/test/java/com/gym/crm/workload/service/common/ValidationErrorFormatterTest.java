package com.gym.crm.workload.service.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationErrorFormatterTest {

    private final ValidationErrorFormatter validationErrorFormatter = new ValidationErrorFormatter();

    @Test
    void shouldFormatViolationsWithGivenDelimiter() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        when(path1.toString()).thenReturn("username");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be blank");
        when(path2.toString()).thenReturn("trainingDuration");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("must be positive");

        String result = validationErrorFormatter.formatViolations(Set.of(violation1, violation2), "; ");

        assertThat(result).isEqualTo("trainingDuration: must be positive; username: must not be blank");
    }

    @Test
    void shouldFormatViolationsFromConstraintViolationException() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        when(exception.getConstraintViolations()).thenReturn(Set.of(violation));

        String result = validationErrorFormatter.formatViolations(exception);

        assertThat(result).isEqualTo("username: must not be blank");
    }

}
