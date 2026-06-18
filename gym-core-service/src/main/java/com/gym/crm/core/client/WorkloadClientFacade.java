package com.gym.crm.core.client;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.ADD;
import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadClientFacade {

    private final WorkloadClient workloadClient;

    public void addWorkload(Training training) {
        updateWorkload(training, ADD);
    }

    public void deleteWorkload(Training training) {
        updateWorkload(training, DELETE);
    }

    private void updateWorkload(Training training, ActionTypeEnum actionType) {
        try {
            Trainer trainer = training.getTrainer();
            User user = trainer.getUser();
            TrainerWorkloadUpdateRequest request = new TrainerWorkloadUpdateRequest()
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .isActive(user.getIsActive())
                    .trainingDate(training.getTrainingDate())
                    .trainingDuration(training.getTrainingDuration())
                    .actionType(actionType);

            log.info("Sending workload update to workload-service: {} (action: {})", request.getUsername(), actionType);
            workloadClient.updateTrainerWorkload(request);
        } catch (Exception e) {
            log.error("Failed to update trainer workload in workload-service for trainer {}: {}",
                    training.getTrainer().getUser().getUsername(), e.getMessage(), e);
        }
    }

}
