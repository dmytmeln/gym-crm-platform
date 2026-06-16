package com.gym.crm.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "Username is required")
        @Size(max = 220, message = "Username must not exceed 220 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 10, message = "Password must be at least 10 characters long")
        String password
) {
}
