package com.gym.crm.workload.contract;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TrainerWorkloadUpdateMessage(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        WorkloadActionType actionType
) {
}
