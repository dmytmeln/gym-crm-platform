package com.gym.crm.core.dto;

import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainingSearchFilter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("passwordUpdateDtoValidProvider")
    void shouldValidatePasswordUpdateDtoIsValid(PasswordUpdateDto dto) {
        Set<ConstraintViolation<PasswordUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("passwordUpdateDtoInvalidProvider")
    void shouldValidatePasswordUpdateDtoHasExpectedError(PasswordUpdateDto dto, String expectedMessage) {
        Set<ConstraintViolation<PasswordUpdateDto>> violations = validator.validate(dto);

        assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .contains(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("trainingSearchFilterValidProvider")
    void shouldValidateTrainingSearchFilterIsValid(TrainingSearchFilter filter) {
        Set<ConstraintViolation<TrainingSearchFilter>> violations = validator.validate(filter);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("trainingSearchFilterInvalidProvider")
    void shouldValidateTrainingSearchFilterHasExpectedError(TrainingSearchFilter filter, String expectedMessage) {
        Set<ConstraintViolation<TrainingSearchFilter>> violations = validator.validate(filter);

        assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .contains(expectedMessage);
    }

    private static Stream<Arguments> passwordUpdateDtoValidProvider() {
        return Stream.of(
                Arguments.of(PasswordUpdateDto.builder().password("SecurePass123").build())
        );
    }

    private static Stream<Arguments> passwordUpdateDtoInvalidProvider() {
        return Stream.of(
                Arguments.of(PasswordUpdateDto.builder().password("short").build(), "Password must be at least 10 characters long"),
                Arguments.of(PasswordUpdateDto.builder().password("").build(), "Password is required"),
                Arguments.of(PasswordUpdateDto.builder().password(null).build(), "Password is required")
        );
    }

    private static Stream<Arguments> trainingSearchFilterValidProvider() {
        return Stream.of(
                Arguments.of(TrainingSearchFilter.builder().username("user").build()),
                Arguments.of(TrainerTrainingSearchFilter.builder().username("user").build()),
                Arguments.of(TraineeTrainingSearchFilter.builder().username("user").build())
        );
    }

    private static Stream<Arguments> trainingSearchFilterInvalidProvider() {
        return Stream.of(
                Arguments.of(TrainingSearchFilter.builder().username("").build(), "Username is required for search"),
                Arguments.of(TrainingSearchFilter.builder().username(null).build(), "Username is required for search"),
                Arguments.of(TrainerTrainingSearchFilter.builder().username("").build(), "Username is required for search"),
                Arguments.of(TrainerTrainingSearchFilter.builder().username(null).build(), "Username is required for search"),
                Arguments.of(TraineeTrainingSearchFilter.builder().username("").build(), "Username is required for search"),
                Arguments.of(TraineeTrainingSearchFilter.builder().username(null).build(), "Username is required for search")
        );
    }

}