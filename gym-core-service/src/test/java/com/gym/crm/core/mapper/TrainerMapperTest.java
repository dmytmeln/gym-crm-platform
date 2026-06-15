package com.gym.crm.core.mapper;

import com.gia.openapi.model.AssignedTraineeResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.factory.TraineeTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_PASSWORD;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_SPECIALIZATION;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithIdAndUserId;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithInactiveStatusAndTrainees;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithTrainees;
import static com.gym.crm.core.factory.TrainerTestFactory.buildUser;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_DATE;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_DURATION;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_NAME;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_TYPE_NAME;
import static com.gym.crm.core.factory.TrainingTestFactory.buildTraining;
import static com.gym.crm.core.factory.TrainingTestFactory.getDefaultTrainingBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerMapperTest {

    private static final String TRAINER_USERNAME = DEFAULT_USERNAME;
    private static final String TRAINER_FIRST_NAME = com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_FIRST_NAME;
    private static final String TRAINER_LAST_NAME = com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_LAST_NAME;

    private TrainerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TrainerMapper.class);
    }

    @Test
    void shouldMapAllFieldsCorrectlyWhenMappingToCreateResponse() {
        Trainer trainer = buildTrainerWithIdAndUserId();

        TrainerCreateResponse result = mapper.toCreateResponse(trainer);

        assertNotNull(result);
        assertEquals(TRAINER_USERNAME, result.getUsername());
        assertEquals(DEFAULT_PASSWORD, result.getPassword());
    }

    @Test
    void shouldReturnNullWhenMappingNullTrainerToCreateResponse() {
        TrainerCreateResponse result = mapper.toCreateResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsIncludingTraineesWhenMappingToGetResponse() {
        Trainer trainer = buildTrainerWithTrainees();

        TrainerGetResponse result = mapper.toGetResponse(trainer);

        assertNotNull(result);
        assertEquals(TRAINER_FIRST_NAME, result.getFirstName());
        assertEquals(TRAINER_LAST_NAME, result.getLastName());
        assertEquals(DEFAULT_SPECIALIZATION, result.getSpecialization());
        assertTrue(result.getIsActive());
        assertNotNull(result.getTrainees());
        assertEquals(1, result.getTrainees().size());
        AssignedTraineeResponse trainee = result.getTrainees().getFirst();
        assertEquals(TraineeTestFactory.DEFAULT_USERNAME, trainee.getUsername());
        assertEquals(TraineeTestFactory.DEFAULT_FIRST_NAME, trainee.getFirstName());
        assertEquals(TraineeTestFactory.DEFAULT_LAST_NAME, trainee.getLastName());
    }

    @Test
    void shouldReturnNullWhenMappingNullTrainerToGetResponse() {
        TrainerGetResponse result = mapper.toGetResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsIncludingTraineesWhenMappingToUpdateResponse() {
        Trainer trainer = buildTrainerWithInactiveStatusAndTrainees();

        TrainerUpdateResponse result = mapper.toUpdateResponse(trainer);

        assertNotNull(result);
        assertEquals(TRAINER_USERNAME, result.getUsername());
        assertEquals(TRAINER_FIRST_NAME, result.getFirstName());
        assertEquals(TRAINER_LAST_NAME, result.getLastName());
        assertEquals(DEFAULT_SPECIALIZATION, result.getSpecialization());
        assertFalse(result.getIsActive());
        assertNotNull(result.getTrainees());
        assertEquals(1, result.getTrainees().size());
        AssignedTraineeResponse trainee = result.getTrainees().getFirst();
        assertEquals(TraineeTestFactory.DEFAULT_USERNAME, trainee.getUsername());
        assertEquals(TraineeTestFactory.DEFAULT_FIRST_NAME, trainee.getFirstName());
        assertEquals(TraineeTestFactory.DEFAULT_LAST_NAME, trainee.getLastName());
    }

    @Test
    void shouldReturnNullWhenMappingNullTrainerToUpdateResponse() {
        TrainerUpdateResponse result = mapper.toUpdateResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsAndIgnoreUserIdUsernamePasswordWhenMappingFromCreateRequest() {
        TrainerCreateRequest request = new TrainerCreateRequest()
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .specialization(DEFAULT_SPECIALIZATION);

        Trainer result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUser().getId());
        assertNull(result.getUser().getUsername());
        assertNull(result.getUser().getPassword());
        assertEquals(TRAINER_FIRST_NAME, result.getUser().getFirstName());
        assertEquals(TRAINER_LAST_NAME, result.getUser().getLastName());
        assertTrue(result.getUser().getIsActive());
        assertEquals(DEFAULT_SPECIALIZATION, result.getSpecialization().getTrainingTypeName());
        assertTrue(result.getTrainings().isEmpty());
        assertTrue(result.getTrainees().isEmpty());
    }

    @Test
    void shouldReturnNullWhenCreateRequestIsNull() {
        Trainer result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllFieldsAndIgnoreIdTrainingsSpecializationWhenMappingFromUpdateRequest() {
        TrainerUpdateRequest request = new TrainerUpdateRequest()
                .firstName("Updated")
                .lastName("Name")
                .isActive(false);

        Trainer result = mapper.toEntity(request, TRAINER_USERNAME);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getSpecialization());
        assertNull(result.getUser().getId());
        assertNull(result.getUser().getPassword());
        assertEquals("Updated", result.getUser().getFirstName());
        assertEquals("Name", result.getUser().getLastName());
        assertEquals(TRAINER_USERNAME, result.getUser().getUsername());
        assertFalse(result.getUser().getIsActive());
        assertTrue(result.getTrainings().isEmpty());
        assertTrue(result.getTrainees().isEmpty());
    }

    @Test
    void shouldReturnNullWhenUpdateRequestIsNull() {
        assertThrows(NullPointerException.class, () -> mapper.toEntity(null, TRAINER_USERNAME));
    }

    @Test
    void shouldMapTraineeToAssignedTraineeResponse() {
        Trainee trainee = Trainee.builder()
                .user(User.builder()
                        .username(TraineeTestFactory.DEFAULT_USERNAME)
                        .firstName(TraineeTestFactory.DEFAULT_FIRST_NAME)
                        .lastName(TraineeTestFactory.DEFAULT_LAST_NAME)
                        .build())
                .build();

        AssignedTraineeResponse result = mapper.toAssignedTraineeResponse(trainee);

        assertNotNull(result);
        assertEquals(TraineeTestFactory.DEFAULT_USERNAME, result.getUsername());
        assertEquals(TraineeTestFactory.DEFAULT_FIRST_NAME, result.getFirstName());
        assertEquals(TraineeTestFactory.DEFAULT_LAST_NAME, result.getLastName());
    }

    @Test
    void shouldReturnNullWhenMappingNullTraineeToAssignedTraineeResponse() {
        AssignedTraineeResponse result = mapper.toAssignedTraineeResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllItemsWhenTraineeSetIsValid() {
        Trainee trainee1 = Trainee.builder()
                .user(buildUser())
                .build();
        Trainee trainee2 = Trainee.builder()
                .user(User.builder().username("liam.miller").firstName("Liam").lastName("Miller").build())
                .build();
        Set<Trainee> trainees = Set.of(trainee1, trainee2);

        List<AssignedTraineeResponse> result = mapper.toAssignedTraineeResponseList(trainees);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenTraineeSetIsEmpty() {
        Set<Trainee> emptySet = Collections.emptySet();

        List<AssignedTraineeResponse> result = mapper.toAssignedTraineeResponseList(emptySet);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnNullWhenTraineeSetIsNull() {
        List<AssignedTraineeResponse> result = mapper.toAssignedTraineeResponseList(null);

        assertNull(result);
    }

    @Test
    void shouldMapTrainingToGetTrainerTrainingResponse() {
        Training training = getDefaultTrainingBuilder()
                .trainee(Trainee.builder().user(buildUser()).build())
                .build();

        GetTrainerTrainingResponse result = mapper.toGetTrainerTrainingResponse(training);

        assertNotNull(result);
        assertEquals(DEFAULT_TRAINING_NAME, result.getTrainingName());
        assertEquals(DEFAULT_TRAINING_TYPE_NAME, result.getTrainingType());
        assertEquals(DEFAULT_USERNAME, result.getTraineeName());
        assertEquals(DEFAULT_DATE, result.getTrainingDate());
        assertEquals(DEFAULT_DURATION, result.getTrainingDuration());
    }

    @Test
    void shouldReturnNullWhenMappingNullTrainingToGetTrainerTrainingResponse() {
        GetTrainerTrainingResponse result = mapper.toGetTrainerTrainingResponse(null);

        assertNull(result);
    }

    @Test
    void shouldMapAllItemsWhenTrainingListIsValid() {
        Training training1 = buildTraining(1L, "Morning HIIT");
        Training training2 = buildTraining(2L, "Evening Yoga");
        List<Training> trainings = List.of(training1, training2);

        List<GetTrainerTrainingResponse> result = mapper.toGetTrainerTrainingResponseList(trainings);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Morning HIIT", result.get(0).getTrainingName());
        assertEquals("Evening Yoga", result.get(1).getTrainingName());
    }

    @Test
    void shouldReturnEmptyListWhenTrainingListIsEmpty() {
        List<Training> emptyList = Collections.emptyList();

        List<GetTrainerTrainingResponse> result = mapper.toGetTrainerTrainingResponseList(emptyList);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnNullWhenTrainingListIsNull() {
        List<GetTrainerTrainingResponse> result = mapper.toGetTrainerTrainingResponseList(null);

        assertNull(result);
    }

}
