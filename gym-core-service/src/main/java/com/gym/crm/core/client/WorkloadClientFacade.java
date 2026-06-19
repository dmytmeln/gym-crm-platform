package com.gym.crm.core.client;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallbackUpdateWorkload")
    public void addWorkload(Training training) {
        updateWorkload(training, ADD);
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallbackUpdateWorkload")
    public void deleteWorkload(Training training) {
        updateWorkload(training, DELETE);
    }

    public void fallbackUpdateWorkload(Training training, Throwable t) {
        log.error("Failed to update trainer workload in workload-service for trainer {}: {}",
                training.getTrainer().getUser().getUsername(), t.getMessage(), t);
    }

    private void updateWorkload(Training training, ActionTypeEnum actionType) {
        User user = training.getTrainer().getUser();
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
    }

}
