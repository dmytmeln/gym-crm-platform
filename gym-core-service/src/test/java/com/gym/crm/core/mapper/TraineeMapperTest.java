package com.gym.crm.core.mapper;

import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_ADDRESS;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_DATE_OF_BIRTH;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_FIRST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_LAST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_PASSWORD;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithIdAndUserId;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_SPECIALIZATION;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithId;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_DATE;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_DURATION;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_ID;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_NAME;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_TYPE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeMapperTest {

    private TraineeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TraineeMapper.class);
    }

    @Test
    void shouldMapToCreateResponseWithUsernameAndPasswordWhenTraineeIsValid() {
        Trainee trainee = buildTraineeWithIdAndUserId();

        TraineeCreateResponse result = mapper.toCreateResponse(trainee);

        assertNotNull(result);
        assertEquals(DEFAULT_USERNAME, result.getUsername());
        assertEquals(DEFAULT_PASSWORD, result.getPassword());
    }

    @Test
    void shouldReturnNullWhenMappingNullTraineeToCreateResponse() {
        TraineeCreateResponse result = mapper.toCreateResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapToGetResponseWithAllFieldsWhenTraineeIsValid() {
        Trainee trainee = buildTraineeWithIdAndUserId();

        TraineeGetResponse result = mapper.toGetResponse(trainee);

        assertNotNull(result);
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName());
        assertEquals(DEFAULT_LAST_NAME, result.getLastName());
        assertEquals(DEFAULT_ADDRESS, result.getAddress());
        assertEquals(DEFAULT_DATE_OF_BIRTH, result.getDateOfBirth());
        assertTrue(result.getIsActive());
    }

    @Test
    void shouldReturnNullWhenMappingNullTraineeToGetResponse() {
        TraineeGetResponse result = mapper.toGetResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapToUpdateResponseWithAllFieldsWhenTraineeIsValid() {
        Trainee trainee = buildTraineeWithIdAndUserId();

        TraineeUpdateResponse result = mapper.toUpdateResponse(trainee);

        assertNotNull(result);
        assertEquals(DEFAULT_USERNAME, result.getUsername());
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName());
        assertEquals(DEFAULT_LAST_NAME, result.getLastName());
        assertEquals(DEFAULT_ADDRESS, result.getAddress());
        assertEquals(DEFAULT_DATE_OF_BIRTH, result.getDateOfBirth());
        assertTrue(result.getIsActive());
    }

    @Test
    void shouldReturnNullWhenMappingNullTraineeToUpdateResponse() {
        TraineeUpdateResponse result = mapper.toUpdateResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsAndIgnoreIdUsernamePasswordWhenMappingFromCreateRequest() {
        TraineeCreateRequest request = new TraineeCreateRequest(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)
                .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
                .address(DEFAULT_ADDRESS);

        Trainee result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUser().getId());
        assertNull(result.getUser().getUsername());
        assertNull(result.getUser().getPassword());
        assertEquals(DEFAULT_FIRST_NAME, result.getUser().getFirstName());
        assertEquals(DEFAULT_LAST_NAME, result.getUser().getLastName());
        assertTrue(result.getUser().getIsActive());
        assertEquals(DEFAULT_ADDRESS, result.getAddress());
        assertEquals(DEFAULT_DATE_OF_BIRTH, result.getDateOfBirth());
    }

    @Test
    void shouldReturnNullWhenCreateRequestIsNull() {
        Trainee result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsAndIgnoreIdUsernamePasswordWhenMappingFromUpdateRequest() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, false)
                .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
                .address(DEFAULT_ADDRESS);

        Trainee result = mapper.toEntity(request, DEFAULT_USERNAME);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUser().getId());
        assertNull(result.getUser().getPassword());
        assertEquals(DEFAULT_FIRST_NAME, result.getUser().getFirstName());
        assertEquals(DEFAULT_LAST_NAME, result.getUser().getLastName());
        assertEquals(false, result.getUser().getIsActive());
        assertEquals(DEFAULT_ADDRESS, result.getAddress());
        assertEquals(DEFAULT_DATE_OF_BIRTH, result.getDateOfBirth());
        assertEquals(DEFAULT_USERNAME, result.getUser().getUsername());
    }

    @Test
    void shouldReturnNullWhenUpdateRequestIsNull() {
        Trainee result = mapper.toEntity(null, null);

        assertNull(result);
    }

    @Test
    void shouldMapTrainerToAssignedTrainerResponse() {
        Trainer trainer = buildTrainerWithId(1L);

        AssignedTrainerResponse result = mapper.toAssignedTrainerResponse(trainer);

        assertNotNull(result);
        assertEquals("marcus.stone", result.getUsername());
        assertEquals("Marcus", result.getFirstName());
        assertEquals("Stone", result.getLastName());
        assertEquals(DEFAULT_SPECIALIZATION, result.getSpecialization());
    }

    @Test
    void shouldMapTrainerListToAssignedTrainerResponseList() {
        Trainer trainer1 = buildTrainerWithId(1L);
        Trainer trainer2 = buildTrainerWithId(2L);
        Set<Trainer> trainers = Set.of(trainer1, trainer2);

        List<AssignedTrainerResponse> result = mapper.toAssignedTrainerResponseList(trainers);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("marcus.stone", result.get(0).getUsername());
        assertEquals("marcus.stone", result.get(1).getUsername());
    }

    @Test
    void shouldReturnEmptyListWhenTrainerListIsEmpty() {
        Set<Trainer> emptySet = Collections.emptySet();

        List<AssignedTrainerResponse> result = mapper.toAssignedTrainerResponseList(emptySet);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMapTrainingToGetTraineeTrainingResponse() {
        Training training = Training.builder()
                .id(DEFAULT_TRAINING_ID)
                .trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).user(User.builder().username("trainer1").build()).build())
                .trainingName(DEFAULT_TRAINING_NAME)
                .trainingType(TrainingType.builder().trainingTypeName(DEFAULT_TRAINING_TYPE_NAME).build())
                .trainingDuration(DEFAULT_DURATION)
                .trainingDate(DEFAULT_DATE)
                .build();

        GetTraineeTrainingResponse result = mapper.toGetTraineeTrainingResponse(training);

        assertNotNull(result);
        assertEquals(DEFAULT_TRAINING_NAME, result.getTrainingName());
        assertEquals(DEFAULT_TRAINING_TYPE_NAME, result.getTrainingType());
        assertEquals(DEFAULT_DURATION, result.getTrainingDuration());
        assertEquals(DEFAULT_DATE, result.getTrainingDate());
        assertEquals("trainer1", result.getTrainerName());
    }

    @Test
    void shouldMapTrainingListToGetTraineeTrainingResponseList() {
        Training training = Training.builder()
                .id(DEFAULT_TRAINING_ID)
                .trainee(Trainee.builder().id(1L).build())
                .trainer(Trainer.builder().id(2L).user(User.builder().username("trainer1").build()).build())
                .trainingName(DEFAULT_TRAINING_NAME)
                .trainingType(TrainingType.builder().trainingTypeName(DEFAULT_TRAINING_TYPE_NAME).build())
                .trainingDuration(DEFAULT_DURATION)
                .trainingDate(DEFAULT_DATE)
                .build();
        List<Training> trainings = List.of(training);

        List<GetTraineeTrainingResponse> result = mapper.toGetTraineeTrainingResponseList(trainings);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DEFAULT_TRAINING_NAME, result.getFirst().getTrainingName());
    }

    @Test
    void shouldReturnEmptyListWhenTrainingListIsEmpty() {
        List<Training> emptyList = Collections.emptyList();

        List<GetTraineeTrainingResponse> result = mapper.toGetTraineeTrainingResponseList(emptyList);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
