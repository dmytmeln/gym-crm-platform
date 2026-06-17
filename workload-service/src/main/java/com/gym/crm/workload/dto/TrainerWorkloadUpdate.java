package com.gym.crm.workload.dto;

public record TrainerWorkloadUpdate(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        TrainingDate trainingDate,
        Integer trainingDuration,
        ActionType actionType
) {
}
