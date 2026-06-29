package com.gym.crm.workload.listener;

import com.gym.crm.logging.TransactionContext;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.mapper.TrainerWorkloadMapper;
import com.gym.crm.workload.service.TrainerWorkloadDeadLetterQueuePublisher;
import com.gym.crm.workload.service.TrainerWorkloadMessageValidator;
import com.gym.crm.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadMessageListener {

    private final TrainerWorkloadDeadLetterQueuePublisher deadLetterQueuePublisher;
    private final TrainerWorkloadMessageValidator messageValidator;
    private final TrainerWorkloadService service;
    private final TrainerWorkloadMapper mapper;

    @JmsListener(destination = "${app.jms.queues.trainer-workload}")
    public void receiveMessage(TrainerWorkloadUpdateMessage message,
                               @Header(name = TRANSACTION_ID, required = false) String transactionId) {
        String resolvedTxId = TransactionContext.resolveTransactionId(transactionId);
        MDC.put(TRANSACTION_ID, resolvedTxId);

        try {
            processUpdate(message, resolvedTxId);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private void processUpdate(TrainerWorkloadUpdateMessage message, String resolvedTransactionId) {
        Optional<String> validationFailureReason = messageValidator.validate(message);

        if (validationFailureReason.isPresent()) {
            log.warn("Sending invalid workload update message to DLQ: {}", validationFailureReason.get());
            deadLetterQueuePublisher.publish(message, validationFailureReason.get(), resolvedTransactionId);

            return;
        }

        log.info("Received workload update message for trainer: {} (action: {})", message.username(), message.actionType());
        TrainerWorkloadUpdate domainUpdate = mapper.toDomainUpdate(message);

        service.updateWorkload(domainUpdate);
    }

}
