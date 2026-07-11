package com.gym.crm.workload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder(toBuilder = true)
public record TrainerWorkloadUpdate(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotNull(message = "Active flag is required")
        Boolean isActive,
        @NotNull(message = "Training date is required")
        TrainingDate trainingDate,
        @NotNull(message = "Training duration is required")
        @Min(value = 0, message = "Training duration must not be negative")
        Integer trainingDuration,
        @NotNull(message = "Action type is required")
        ActionType actionType
) {
}
