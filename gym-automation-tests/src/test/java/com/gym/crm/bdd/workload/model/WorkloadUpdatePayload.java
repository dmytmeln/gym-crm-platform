package com.gym.crm.bdd.workload.model;

import java.time.LocalDate;

public record WorkloadUpdatePayload(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        WorkloadAction actionType
) {
}
