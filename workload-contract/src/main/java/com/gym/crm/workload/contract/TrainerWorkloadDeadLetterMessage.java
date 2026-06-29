package com.gym.crm.workload.contract;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record TrainerWorkloadDeadLetterMessage(
        TrainerWorkloadUpdateMessage originalMessage,
        String failureReason,
        String transactionId
) implements Serializable {
}
