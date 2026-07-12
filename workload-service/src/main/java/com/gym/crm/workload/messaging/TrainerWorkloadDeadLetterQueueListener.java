package com.gym.crm.workload.messaging;

import com.gym.crm.logging.TransactionContext;
import com.gym.crm.workload.contract.TrainerWorkloadDeadLetterMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;

@Component
@Slf4j
public class TrainerWorkloadDeadLetterQueueListener {

    @JmsListener(destination = "${app.jms.queues.trainer-workload-dlq}",
            containerFactory = "trainerWorkloadDeadLetterQueueListenerContainerFactory")
    public void receiveMessage(TrainerWorkloadDeadLetterMessage message,
                               @Header(name = TRANSACTION_ID, required = false) String transactionId) {
        String finalTransactionId = Optional.ofNullable(transactionId)
                .orElse(message.transactionId());
        String resolvedTransactionId = TransactionContext.resolveTransactionId(finalTransactionId);
        MDC.put(TRANSACTION_ID, resolvedTransactionId);

        try {
            log.error("Consumed workload dead letter message for trainer: {} (action: {}, failureReason: {}, transactionId: {})",
                    message.originalMessage().username(),
                    message.originalMessage().actionType(),
                    message.failureReason(),
                    resolvedTransactionId);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

}
