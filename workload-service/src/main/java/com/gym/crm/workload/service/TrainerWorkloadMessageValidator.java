package com.gym.crm.workload.service;

import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.util.ValidationUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageValidator {

    private static final String VIOLATION_DELIMITER = "; ";

    private final Validator validator;

    public Optional<String> validate(TrainerWorkloadUpdateMessage message) {
        Set<ConstraintViolation<TrainerWorkloadUpdateMessage>> constraintViolations = validator.validate(message);

        if (constraintViolations.isEmpty()) {
            return Optional.empty();
        }

        String formattedViolations = ValidationUtils.formatViolations(constraintViolations, VIOLATION_DELIMITER);

        return Optional.of(formattedViolations);
    }

}
