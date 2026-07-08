package com.gym.crm.workload.model;

import com.gym.crm.workload.dto.TrainingDate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassValidationWhenWorkloadIsValid() {
        TrainerWorkload valid = buildTrainerWorkload();

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(valid);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidTrainerWorkloads")
    void shouldFailValidationWhenSavingInvalidWorkload(TrainerWorkload invalid) {
        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(invalid);

        assertThat(violations).isNotEmpty();
    }

    private static Stream<TrainerWorkload> invalidTrainerWorkloads() {
        TrainerWorkload base = buildTrainerWorkload();
        YearSummary firstYear = base.getYears().getFirst();
        MonthSummary firstMonth = firstYear.getMonths().getFirst();

        YearSummary invalidYear = firstYear.toBuilder()
                .year(1899)
                .build();
        MonthSummary invalidMonth = firstMonth.toBuilder()
                .workingHours(-10)
                .build();
        YearSummary yearWithInvalidMonth = firstYear.toBuilder()
                .months(List.of(invalidMonth))
                .build();

        return Stream.of(base.toBuilder().username("").build(),
                base.toBuilder().username(null).build(),
                base.toBuilder().firstName("").build(),
                base.toBuilder().firstName(null).build(),
                base.toBuilder().lastName("").build(),
                base.toBuilder().lastName(null).build(),
                base.toBuilder().isActive(null).build(),
                base.toBuilder().years(null).build(),
                base.toBuilder().years(List.of(invalidYear)).build(),
                base.toBuilder().years(List.of(yearWithInvalidMonth)).build());
    }

    private static TrainerWorkload buildTrainerWorkload() {
        YearSummary year = YearSummary.of(TrainingDate.of(2026, JANUARY), 120);
        List<YearSummary> yearSummaries = List.of(year);

        return TrainerWorkload.builder()
                .username("marcus.stone")
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .years(yearSummaries)
                .build();
    }

}

