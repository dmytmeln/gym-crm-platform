package com.gym.crm.workload.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.util.Optional;

@Component
@Slf4j
public class TrainerWorkloadListenerErrorHandler implements ErrorHandler {

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Optional<TrainerWorkloadProcessingException> processingException = extractProcessingException(throwable);

        if (processingException.isEmpty()) {
            log.error("Failed before trainer workload listener method execution", throwable);
            return;
        }

        log.error("Failed to process workload update message for trainer: {} (action: {}, transactionId: {})",
                processingException.get().getTrainerUsername(),
                processingException.get().getActionType(),
                processingException.get().getTransactionId(),
                processingException.get());
    }

    private Optional<TrainerWorkloadProcessingException> extractProcessingException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TrainerWorkloadProcessingException processingException) {
                return Optional.of(processingException);
            }

            current = current.getCause();
        }

        return Optional.empty();
    }

}
