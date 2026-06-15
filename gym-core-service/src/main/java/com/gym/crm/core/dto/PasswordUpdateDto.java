package com.gym.crm.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PasswordUpdateDto(
        @NotBlank(message = "Password is required")
        @Size(min = 10, message = "Password must be at least 10 characters long")
        String password
) {
}
