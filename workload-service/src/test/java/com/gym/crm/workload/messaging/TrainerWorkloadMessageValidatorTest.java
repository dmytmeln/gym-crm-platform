package com.gym.crm.workload.messaging;

import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.service.common.ValidationErrorFormatter;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadMessageValidatorTest {

    private static Validator validator;

    private TrainerWorkloadMessageValidator messageValidator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void init() {
        ValidationErrorFormatter validationErrorFormatter = new ValidationErrorFormatter();
        messageValidator = new TrainerWorkloadMessageValidator(validator, validationErrorFormatter);
    }

    @Test
    void shouldReturnEmptyWhenMessageIsValid() {
        TrainerWorkloadUpdateMessage message = buildValidMessage();

        Optional<String> actual = messageValidator.validate(message);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnSortedViolationMessageWhenMessageIsInvalid() {
        TrainerWorkloadUpdateMessage message = TrainerWorkloadUpdateMessage.builder()
                .username(null)
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(null)
                .actionType(ADD)
                .build();

        Optional<String> actual = messageValidator.validate(message);

        assertThat(actual).contains("trainingDuration: Training duration is required; username: Username is required");
    }

    private TrainerWorkloadUpdateMessage buildValidMessage() {
        return TrainerWorkloadUpdateMessage.builder()
                .username("trainer.user")
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(60)
                .actionType(ADD)
                .build();
    }

}
