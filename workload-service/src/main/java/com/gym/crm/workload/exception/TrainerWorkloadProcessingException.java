package com.gym.crm.workload.exception;

import com.gym.crm.workload.contract.WorkloadActionType;
import lombok.Getter;

@Getter
public class TrainerWorkloadProcessingException extends RuntimeException {

    private final String transactionId;
    private final String trainerUsername;
    private final WorkloadActionType actionType;

    public TrainerWorkloadProcessingException(String transactionId,
                                              String trainerUsername,
                                              WorkloadActionType actionType,
                                              Throwable cause) {
        super("Failed to process trainer workload message", cause);
        this.transactionId = transactionId;
        this.trainerUsername = trainerUsername;
        this.actionType = actionType;
    }

}
