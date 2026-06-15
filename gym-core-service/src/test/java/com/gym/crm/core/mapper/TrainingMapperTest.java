package com.gym.crm.core.mapper;

import com.gia.openapi.model.TrainingCreateRequest;
import com.gym.crm.core.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingMapperTest {

    private static final String TRAINEE_USERNAME = "billy.herrington";
    private static final String TRAINER_USERNAME = "ricardo.milos";
    private static final String TRAINING_NAME = "Morning Workout";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2026, 4, 15);
    private static final Integer TRAINING_DURATION = 60;

    private TrainingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TrainingMapper.class);
    }

    @Test
    void shouldMapAllFieldsCorrectlyWhenRequestIsValid() {
        TrainingCreateRequest request = buildTrainingCreateRequest();

        Training result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getTrainingType());
        assertNotNull(result.getTrainee());
        assertEquals(TRAINEE_USERNAME, result.getTrainee().getUser().getUsername());
        assertNotNull(result.getTrainer());
        assertEquals(TRAINER_USERNAME, result.getTrainer().getUser().getUsername());
        assertEquals(TRAINING_NAME, result.getTrainingName());
        assertEquals(TRAINING_DATE, result.getTrainingDate());
        assertEquals(TRAINING_DURATION, result.getTrainingDuration());
    }

    @Test
    void shouldReturnNullWhenRequestIsNull() {
        Training result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void shouldMapTraineeUsernameWhenOnlyTraineeIsProvided() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        request.setTrainerUsername(null);

        Training result = mapper.toEntity(request);

        assertNotNull(result);
        assertNotNull(result.getTrainee());
        assertEquals(TRAINEE_USERNAME, result.getTrainee().getUser().getUsername());
        assertNotNull(result.getTrainer());
        assertNull(result.getTrainer().getUser().getUsername());
    }

    @Test
    void shouldMapTrainerUsernameWhenOnlyTrainerIsProvided() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        request.setTraineeUsername(null);

        Training result = mapper.toEntity(request);

        assertNotNull(result);
        assertNotNull(result.getTrainee());
        assertNull(result.getTrainee().getUser().getUsername());
        assertNotNull(result.getTrainer());
        assertEquals(TRAINER_USERNAME, result.getTrainer().getUser().getUsername());
    }

    @Test
    void shouldIgnoreIdWhenMappingFromRequest() {
        TrainingCreateRequest request = buildTrainingCreateRequest();

        Training result = mapper.toEntity(request);

        assertNull(result.getId());
    }

    @Test
    void shouldIgnoreTrainingTypeWhenMappingFromRequest() {
        TrainingCreateRequest request = buildTrainingCreateRequest();

        Training result = mapper.toEntity(request);

        assertNull(result.getTrainingType());
    }

    private TrainingCreateRequest buildTrainingCreateRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTrainingName(TRAINING_NAME);
        request.setTrainingDate(TRAINING_DATE);
        request.setTrainingDuration(TRAINING_DURATION);

        return request;
    }

}
