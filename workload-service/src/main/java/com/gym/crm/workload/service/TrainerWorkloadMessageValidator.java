package com.gym.crm.workload.service;

import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageValidator {

    private static final String VIOLATION_MESSAGE_FORMAT = "%s: %s";
    private static final String VIOLATION_DELIMITER = "; ";

    private final Validator validator;

    public Optional<String> validate(TrainerWorkloadUpdateMessage message) {
        Set<ConstraintViolation<TrainerWorkloadUpdateMessage>> constraintViolations = validator.validate(message);

        if (constraintViolations.isEmpty()) {
            return Optional.empty();
        }

        String formattedViolations = constraintViolations.stream()
                .filter(violation -> violation.getPropertyPath() != null && violation.getMessage() != null)
                .sorted(comparing(violation -> violation.getPropertyPath().toString()))
                .map(this::formatViolation)
                .collect(joining(VIOLATION_DELIMITER));

        return Optional.of(formattedViolations);
    }

    private String formatViolation(ConstraintViolation<TrainerWorkloadUpdateMessage> violation) {
        String path = violation.getPropertyPath().toString();
        String message = violation.getMessage();

        return VIOLATION_MESSAGE_FORMAT.formatted(path, message);
    }

}
