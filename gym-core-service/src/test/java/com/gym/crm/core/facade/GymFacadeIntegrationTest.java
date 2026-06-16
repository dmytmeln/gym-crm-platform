package com.gym.crm.core.facade;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.config.MySqlContainerTestConfig;
import com.gym.crm.core.config.TestDataset;
import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.AUGUST;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestDataset
class GymFacadeIntegrationTest {

    private static final String TRAINEE_USERNAME = "liam.miller";
    private static final String TRAINER_USERNAME = "marcus.stone";
    private static final String TRAINING_NAME = "Morning HIIT";

    @Autowired
    private GymFacade facade;

    @DynamicPropertySource
    static void setMySqlProperties(DynamicPropertyRegistry registry) {
        MySqlContainerTestConfig.setMySqlContainerProperties(registry);
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldGetTraineeByUsername() {
        TraineeGetResponse actual = facade.getTraineeByUsername(TRAINEE_USERNAME);

        assertNotNull(actual);
        assertEquals("Liam", actual.getFirstName());
        assertEquals("Miller", actual.getLastName());
        assertTrue(actual.getIsActive());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldGetAvailableTrainersForTrainee() {
        List<AssignedTrainerResponse> actual = facade.getAvailableTrainersForTrainee(TRAINEE_USERNAME);

        assertEquals(2, actual.size());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldGetTraineeTrainings() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(TRAINEE_USERNAME)
                .build();

        List<GetTraineeTrainingResponse> actual = facade.getTraineeTrainings(filter);

        assertEquals(1, actual.size());
        assertEquals("Morning HIIT", actual.getFirst().getTrainingName());
    }

    @Test
    void shouldCreateTrainee() {
        TraineeCreateRequest request = new TraineeCreateRequest("Liam", "Miller")
                .dateOfBirth(LocalDate.of(1990, JANUARY, 1))
                .address("123 Test St");

        TraineeCreateResponse actual = facade.createTrainee(request);

        assertNotNull(actual.getUsername());
        assertNotNull(actual.getPassword());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldUpdateTrainee() {
        TraineeUpdateRequest request = new TraineeUpdateRequest("Updated", "Name", true)
                .address("Updated Address")
                .dateOfBirth(LocalDate.of(1995, MAY, 15));

        TraineeUpdateResponse actual = facade.updateTrainee(TRAINEE_USERNAME, request);

        assertEquals("Updated", actual.getFirstName());
        assertEquals("Name", actual.getLastName());
        assertTrue(actual.getIsActive());
        assertEquals("Updated Address", actual.getAddress());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldUpdateTraineeTrainers() {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest()
                .trainerUsernames(List.of("sarah.adams", "alex.morgan"));

        facade.updateTraineeTrainers(TRAINEE_USERNAME, request);

        TraineeGetResponse trainee = facade.getTraineeByUsername(TRAINEE_USERNAME);
        assertEquals(2, trainee.getTrainers().size());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldUpdateTraineeActivationStatus() {
        ActivationStatusRequest request = new ActivationStatusRequest(false);

        facade.updateTraineeActivationStatus(TRAINEE_USERNAME, request);

        TraineeGetResponse trainee = facade.getTraineeByUsername(TRAINEE_USERNAME);
        assertFalse(trainee.getIsActive());
    }

    @Test
    @WithMockUser(username = TRAINEE_USERNAME, roles = "TRAINEE")
    void shouldDeleteTraineeByUsername() {
        boolean actual = facade.deleteTraineeByUsername(TRAINEE_USERNAME);

        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldGetTrainerByUsername() {
        TrainerGetResponse actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertNotNull(actual);
        assertEquals("Marcus", actual.getFirstName());
        assertEquals("Stone", actual.getLastName());
    }

    @Test
    void shouldCreateTrainer() {
        TrainerCreateRequest request = new TrainerCreateRequest()
                .firstName("Liam")
                .lastName("Miller")
                .specialization("CARDIO");

        TrainerCreateResponse actual = facade.createTrainer(request);

        assertNotNull(actual.getUsername());
        assertNotNull(actual.getPassword());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldUpdateTrainer() {
        TrainerUpdateRequest request = new TrainerUpdateRequest()
                .firstName("Updated")
                .lastName("Name")
                .isActive(false);

        TrainerUpdateResponse actual = facade.updateTrainer(TRAINER_USERNAME, request);

        assertEquals("Updated", actual.getFirstName());
        assertEquals("Name", actual.getLastName());
        assertFalse(actual.getIsActive());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldUpdateTrainerActivationStatus() {
        facade.updateTrainerActivationStatus(TRAINER_USERNAME, false);

        facade.updateTrainerActivationStatus(TRAINER_USERNAME, true);

        TrainerGetResponse trainer = facade.getTrainerByUsername(TRAINER_USERNAME);
        assertNotNull(trainer);
        assertTrue(trainer.getIsActive());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldCreateTraining() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTrainingName("Evening Yoga");
        request.setTrainingDate(LocalDate.of(2025, AUGUST, 15));
        request.setTrainingDuration(45);

        facade.createTraining(request);

        TrainerTrainingSearchFilter searchFilter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .build();
        List<GetTrainerTrainingResponse> trainings = facade.getTrainerTrainings(searchFilter);
        assertNotNull(trainings);
        assertEquals(2, trainings.size());
        assertTrue(trainings.stream().anyMatch(t -> "Evening Yoga".equals(t.getTrainingName())));
    }

    @Test
    void shouldGetAllTrainingTypes() {
        List<TrainingTypeResponse> actual = facade.getAllTrainingTypes();

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldGetTrainerTrainings() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .build();

        List<GetTrainerTrainingResponse> actual = facade.getTrainerTrainings(filter);

        assertEquals(1, actual.size());
        assertEquals(TRAINING_NAME, actual.getFirst().getTrainingName());
    }

    @Test
    @WithMockUser(username = TRAINER_USERNAME, roles = "TRAINER")
    void shouldGetEmptyTrainerTrainingsWhenFilterDoesNotMatch() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .traineeName("non.existent")
                .build();

        List<GetTrainerTrainingResponse> actual = facade.getTrainerTrainings(filter);

        assertTrue(actual.isEmpty());
    }

}
