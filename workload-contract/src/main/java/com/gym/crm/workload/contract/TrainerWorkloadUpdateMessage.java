package com.gym.crm.workload.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TrainerWorkloadUpdateMessage(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotNull(message = "Active flag is required")
        Boolean isActive,
        @NotNull(message = "Training date is required")
        LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        Integer trainingDuration,
        @NotNull(message = "Action type is required")
        WorkloadActionType actionType
) {
}
