package com.gym.crm.workload.dto;

import lombok.Builder;

@Builder(toBuilder = true)
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
