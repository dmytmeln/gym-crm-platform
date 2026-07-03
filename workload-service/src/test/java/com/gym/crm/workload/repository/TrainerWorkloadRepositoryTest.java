package com.gym.crm.workload.repository;

import com.gym.crm.workload.config.MongoContainerTestConfig;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class TrainerWorkloadRepositoryTest {

    private static final String USERNAME = "marcus.stone";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TrainerWorkloadRepository repository;

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerTestConfig.setMongoContainerProperties(registry);
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(TrainerWorkload.class);
    }

    @Test
    void shouldFindByUsernameWhenExists() {
        TrainerWorkload expected = buildTrainerWorkload();
        mongoTemplate.save(expected);

        Optional<TrainerWorkload> actual = repository.findByUsername(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsername() {
        Optional<TrainerWorkload> actual = repository.findByUsername("nonexistent.trainer");

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveWorkloadCorrectly() {
        TrainerWorkload expected = buildTrainerWorkload();

        repository.save(expected);

        TrainerWorkload actual = mongoTemplate.findById(USERNAME, TrainerWorkload.class);
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private TrainerWorkload buildTrainerWorkload() {
        YearSummary year = YearSummary.of(TrainingDate.of(2026, JANUARY), 120);
        List<YearSummary> yearSummaries = List.of(year);

        return TrainerWorkload.builder()
                .username(USERNAME)
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .years(yearSummaries)
                .build();
    }

}
