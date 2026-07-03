package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.config.MongoContainerTestConfig;
import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Month;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.workload.dto.ActionType.ADD;
import static com.gym.crm.workload.dto.ActionType.DELETE;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@Import(TrainerWorkloadServiceImpl.class)
class TrainerWorkloadServiceImplIntegrationTest {

    private static final String USERNAME = "marcus.stone";
    private static final int DEFAULT_DURATION = 60;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TrainerWorkloadService service;

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerTestConfig.setMongoContainerProperties(registry);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateNewWorkloadOnAddWhenNotExist() {
        int year = 2025;
        Month month = JUNE;
        int trainingDuration = 60;
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(year, month), trainingDuration, ADD);
        YearSummary yearSummary = buildYearSummary(year, MonthSummary.of(month, trainingDuration));
        TrainerWorkload expected = buildTrainerWorkload(yearSummary);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
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
        saveTrainerWorkload();
        int trainingDuration = 45;
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JUNE), trainingDuration, ADD);
        int expectedDuration = DEFAULT_DURATION + trainingDuration;

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(expectedDuration);
    }

    @Test
    void shouldAddNewMonthOnAddForExistingYear() {
        saveTrainerWorkload();
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2025, JULY), 45, ADD);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths()).hasSize(2);
    }

    @Test
    void shouldAddNewYearOnAddForExistingTrainer() {
        saveTrainerWorkload();
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(2026, JUNE), 45, ADD);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(2);
    }

    @Test
    void shouldSubtractHoursOnDeleteForExistingMonth() {
        int initialDuration = 100;
        int year = 2025;
        Month month = JUNE;
        YearSummary yearSummary = buildYearSummary(year, MonthSummary.of(month, initialDuration));
        saveTrainerWorkload(yearSummary);
        int updateDuration = 40;
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(year, month), updateDuration, DELETE);
        int expectedDuration = initialDuration - updateDuration;

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(expectedDuration);
    }

    @Test
    void shouldDeleteMonthAndYearOnDeleteWhenHoursBecomeZeroOrLess() {
        int year = 2025;
        Month month = JUNE;
        YearSummary yearSummary = buildYearSummary(year, MonthSummary.of(month, 50));
        saveTrainerWorkload(yearSummary);
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(year, month), 60, DELETE);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).isEmpty();
    }

    @Test
    void shouldDoNothingOnDeleteForNonExistentMonth() {
        int juneTrainingDuration = 50;
        int year = 2025;
        YearSummary yearSummary = buildYearSummary(year, MonthSummary.of(JUNE, juneTrainingDuration));
        saveTrainerWorkload(yearSummary);
        TrainerWorkloadUpdate update = buildUpdate(TrainingDate.of(year, JULY), 30, DELETE);

        service.updateWorkload(update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears().getFirst().getMonths()).hasSize(1);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getMonth()).isEqualTo(JUNE);
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(juneTrainingDuration);
    }

    @Test
    void shouldGetWorkingHoursWhenExist() {
        saveTrainerWorkload();
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2025, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isEqualTo(DEFAULT_DURATION);
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenNotExist() {
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter("nonexistent.trainer", 2025, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldUpdateTrainerInfoOnUpdateWhenExists() {
        saveTrainerWorkload();
        String newFirstName = "NewFirstName";
        String newLastName = "NewLastName";
        int updateTrainingDuration = 40;
        TrainerWorkloadUpdate updatedInfo = buildUpdate(newFirstName, newLastName, false,
                TrainingDate.of(2025, JUNE), updateTrainingDuration, ADD);
        int expectedDuration = DEFAULT_DURATION + updateTrainingDuration;

        service.updateWorkload(updatedInfo);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getFirstName()).isEqualTo(newFirstName);
        assertThat(actual.getLastName()).isEqualTo(newLastName);
        assertThat(actual.getIsActive()).isFalse();
        assertThat(actual.getYears().getFirst().getMonths().getFirst().getWorkingHours()).isEqualTo(expectedDuration);
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenYearDoesNotExist() {
        saveTrainerWorkload();
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 1111, JUNE);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenMonthDoesNotExist() {
        saveTrainerWorkload();
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2025, JULY);

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldDeleteOnlyTargetMonthLeaveOthersIntact() {
        int juneTrainingDuration = 50;
        MonthSummary juneMonthSummary = MonthSummary.of(JUNE, juneTrainingDuration);
        MonthSummary julyMonthSummary = MonthSummary.of(JULY, 40);
        int year = 2025;
        YearSummary yearSummaries = buildYearSummary(year, juneMonthSummary, julyMonthSummary);
        saveTrainerWorkload(yearSummaries);
        TrainerWorkloadUpdate deleteJuneUpdate = buildUpdate(TrainingDate.of(year, JUNE), juneTrainingDuration, DELETE);

        service.updateWorkload(deleteJuneUpdate);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(1);
        YearSummary yearSummary = actual.getYears().getFirst();
        assertThat(yearSummary.getYear()).isEqualTo(year);
        assertThat(yearSummary.getMonths()).hasSize(1);
        assertThat(yearSummary.getMonths().getFirst().getMonth()).isEqualTo(JULY);
    }

    @Test
    void shouldDeleteOnlyTargetYearLeaveOthersIntact() {
        YearSummary yearSummary2025 = buildYearSummary(2025, MonthSummary.of(JUNE, 50));
        int year = 2026;
        Month month = JUNE;
        int duration = 30;
        YearSummary yearSummary2026 = buildYearSummary(year, MonthSummary.of(month, duration));
        saveTrainerWorkload(yearSummary2025, yearSummary2026);
        TrainerWorkloadUpdate delete2026Update = buildUpdate(TrainingDate.of(year, month), duration, DELETE);

        service.updateWorkload(delete2026Update);

        TrainerWorkload actual = repository.findByUsername(USERNAME).orElseThrow();
        assertThat(actual.getYears()).hasSize(1);
        assertThat(actual.getYears().getFirst().getYear()).isEqualTo(2025);
    }

    private TrainerWorkload buildTrainerWorkload() {
        YearSummary yearSummary = buildYearSummary(2025, MonthSummary.of(JUNE, DEFAULT_DURATION));
        return buildTrainerWorkload(yearSummary);
    }

    private TrainerWorkload buildTrainerWorkload(YearSummary... years) {
        return TrainerWorkload.builder()
                .username(USERNAME)
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .years(List.of(years))
                .build();
    }

    private void saveTrainerWorkload() {
        TrainerWorkload trainerWorkload = buildTrainerWorkload();
        repository.save(trainerWorkload);
    }

    private void saveTrainerWorkload(YearSummary... years) {
        TrainerWorkload trainerWorkload = buildTrainerWorkload(years);
        repository.save(trainerWorkload);
    }

    private YearSummary buildYearSummary(int year, MonthSummary... months) {
        return YearSummary.builder()
                .year(year)
                .months(List.of(months))
                .build();
    }

    private TrainerWorkloadUpdate buildUpdate(TrainingDate trainingDate, int trainingDuration, ActionType actionType) {
        return buildUpdate("Marcus", "Stone", true, trainingDate, trainingDuration, actionType);
    }

    private TrainerWorkloadUpdate buildUpdate(String firstName, String lastName, boolean isActive,
                                              TrainingDate trainingDate, int trainingDuration, ActionType actionType) {
        return TrainerWorkloadUpdate.builder()
                .username(USERNAME)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(isActive)
                .trainingDate(trainingDate)
                .trainingDuration(trainingDuration)
                .actionType(actionType)
                .build();
    }

}
