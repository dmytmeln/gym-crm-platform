package com.gym.crm.core.client;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.ADD;
import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.DELETE;
import static java.time.Month.JUNE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadClientFacadeTest {

    @Mock
    private WorkloadClient workloadClient;

    @InjectMocks
    private WorkloadClientFacade facade;

    @Test
    void shouldCallWorkloadClientSuccessfullyWhenAddAction() {
        Training training = buildTestTraining();

        facade.addWorkload(training);

        ArgumentCaptor<TrainerWorkloadUpdateRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadUpdateRequest.class);
        verify(workloadClient).updateTrainerWorkload(captor.capture());
        TrainerWorkloadUpdateRequest request = captor.getValue();
        assertEquals("trainer.user", request.getUsername());
        assertEquals("First", request.getFirstName());
        assertEquals("Last", request.getLastName());
        assertEquals(true, request.getIsActive());
        assertEquals(training.getTrainingDate(), request.getTrainingDate());
        assertEquals(training.getTrainingDuration(), request.getTrainingDuration());
        assertEquals(ADD, request.getActionType());
    }

    @Test
    void shouldCallWorkloadClientSuccessfullyWhenDeleteAction() {
        Training training = buildTestTraining();

        facade.deleteWorkload(training);

        ArgumentCaptor<TrainerWorkloadUpdateRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadUpdateRequest.class);
        verify(workloadClient).updateTrainerWorkload(captor.capture());
        TrainerWorkloadUpdateRequest request = captor.getValue();
        assertEquals(DELETE, request.getActionType());
    }

    @Test
    void shouldLogAndNotPropagateExceptionWhenWorkloadClientThrows() {
        Training training = buildTestTraining();

        doThrow(new RuntimeException("API error")).when(workloadClient).updateTrainerWorkload(any());

        facade.addWorkload(training);

        verify(workloadClient).updateTrainerWorkload(any());
    }

    private Training buildTestTraining() {
        User user = User.builder()
                .username("trainer.user")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, JUNE, 17))
                .trainingDuration(60)
                .build();
    }

}

