package com.gym.crm.workload.dto;

import java.time.Month;
import lombok.Builder;

@Builder
public record TrainerWorkloadSearchFilter(
        String username,
        Integer year,
        Month month
) {
}
