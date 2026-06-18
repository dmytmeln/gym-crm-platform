package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.repository.impl.InMemoryTrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.gym.crm.workload.dto.ActionType.ADD;
import static com.gym.crm.workload.dto.ActionType.DELETE;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadServiceImplIntegrationTest {

    private static final String USERNAME = "marcus.stone";

    private TrainerWorkloadRepository repository;
    private TrainerWorkloadService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrainerWorkloadRepository();
        service = new TrainerWorkloadServiceImpl(repository);
    }

    @Test
    void shouldCreateNewWorkloadOnAddWhenNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getFirstName()).isEqualTo("Marcus");
        assertThat(actual.getLastName()).isEqualTo("Stone");
        assertThat(actual.getIsActive()).isTrue();
        assertThat(actual.getYears()).hasSize(1);
        assertThat(actual.getYears().getFirst().getYear()).isEqualTo(2025);
        assertThat(actual.getYears().getFirst().getMonths()).hasSize(1);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getMonth()).isEqualTo(JUNE);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(60);
    }

    @Test
    void shouldDoNothingOnDeleteWhenNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), 60, DELETE);

        service.updateWorkload(update);

        Optional<TrainerWorkload> actual = repository.findByUsername(USERNAME);
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldAccumulateHoursOnAddForExistingMonth() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2025, JUNE), 45, ADD);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(105);
    }

    @Test
    void shouldAddNewMonthOnAddForExistingYear() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2025, JULY), 45, ADD);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths()).hasSize(2);
    }

    @Test
    void shouldAddNewYearOnAddForExistingTrainer() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2026, JUNE), 45, ADD);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(2);
    }

    @Test
    void shouldSubtractHoursOnDeleteForExistingMonth() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 100, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2025, JUNE), 40, DELETE);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(60);
    }

    @Test
    void shouldDeleteMonthAndYearOnDeleteWhenHoursBecomeZeroOrLess() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 50, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2025, JUNE), 60, DELETE);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).isEmpty();
    }

    @Test
    void shouldDoNothingOnDeleteForNonExistentMonth() {
        TrainerWorkloadUpdate update1 = buildUpdate(TrainingDate.of(2025, JUNE), 50, ADD);
        TrainerWorkloadUpdate update2 = buildUpdate(TrainingDate.of(2025, JULY), 30, DELETE);
        service.updateWorkload(update1);

        service.updateWorkload(update2);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths()).hasSize(1);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getMonth()).isEqualTo(JUNE);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(50);
    }

    @Test
    void shouldGetWorkingHoursWhenExist() {
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        service.updateWorkload(update);
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2025, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isEqualTo(60);
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenNotExist() {
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter("nonexistent.trainer", 2025, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldUpdateTrainerInfoOnUpdateWhenExists() {
        TrainerWorkloadUpdate initial = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        service.updateWorkload(initial);
        TrainerWorkloadUpdate updatedInfo = new TrainerWorkloadUpdate(USERNAME, "NewFirstName", "NewLastName", false,
                TrainingDate.of(2025, JUNE), 40, ADD);

        service.updateWorkload(updatedInfo);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getFirstName()).isEqualTo("NewFirstName");
        assertThat(actual.getLastName()).isEqualTo("NewLastName");
        assertThat(actual.getIsActive()).isFalse();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(100);
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenYearDoesNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        service.updateWorkload(update);
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2026, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenMonthDoesNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), 60, ADD);
        service.updateWorkload(update);
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2025, JULY);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldDeleteOnlyTargetMonthLeaveOthersIntact() {
        TrainerWorkloadUpdate addJune = buildUpdate(TrainingDate.of(2025, JUNE), 50, ADD);
        TrainerWorkloadUpdate addJuly = buildUpdate(TrainingDate.of(2025, JULY), 40, ADD);
        service.updateWorkload(addJune);
        service.updateWorkload(addJuly);
        TrainerWorkloadUpdate deleteJune = buildUpdate(TrainingDate.of(2025, JUNE), 50, DELETE);

        service.updateWorkload(deleteJune);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(1);
        YearSummary yearSummary = actual.getYears().getFirst();
        assertThat(yearSummary.getYear()).isEqualTo(2025);
        assertThat(yearSummary.getMonths()).hasSize(1);
        assertThat(yearSummary.getMonths().getFirst().getMonth()).isEqualTo(JULY);
    }

    @Test
    void shouldDeleteOnlyTargetYearLeaveOthersIntact() {
        TrainerWorkloadUpdate add2025 = buildUpdate(TrainingDate.of(2025, JUNE), 50, ADD);
        TrainerWorkloadUpdate add2026 = buildUpdate(TrainingDate.of(2026, JUNE), 30, ADD);
        service.updateWorkload(add2025);
        service.updateWorkload(add2026);
        TrainerWorkloadUpdate delete2026 = buildUpdate(TrainingDate.of(2026, JUNE), 30, DELETE);

        service.updateWorkload(delete2026);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(1);
        assertThat(actual.getYears().getFirst().getYear()).isEqualTo(2025);
    }

    private TrainerWorkloadUpdate buildUpdate(TrainingDate trainingDate, int trainingDuration, ActionType actionType) {
        return new TrainerWorkloadUpdate(USERNAME, "Marcus", "Stone", true,
                trainingDate, trainingDuration, actionType);
    }

}
