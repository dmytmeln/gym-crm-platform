package com.gym.crm.workload.messaging;

import com.gym.crm.workload.contract.TrainerWorkloadDeadLetterMessage;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;

@Component
public class TrainerWorkloadDeadLetterQueuePublisher {

    private final JmsTemplate jmsTemplate;
    private final String trainerWorkloadDeadLetterQueue;

    public TrainerWorkloadDeadLetterQueuePublisher(JmsTemplate jmsTemplate,
                                                   @Value("${app.jms.queues.trainer-workload-dlq}")
                                                   String trainerWorkloadDeadLetterQueue) {
        this.jmsTemplate = jmsTemplate;
        this.trainerWorkloadDeadLetterQueue = trainerWorkloadDeadLetterQueue;
    }

    public void publish(TrainerWorkloadUpdateMessage originalMessage, String failureReason, String transactionId) {
        TrainerWorkloadDeadLetterMessage deadLetterMessage = TrainerWorkloadDeadLetterMessage.builder()
                .originalMessage(originalMessage)
                .failureReason(failureReason)
                .transactionId(transactionId)
                .build();

        jmsTemplate.convertAndSend(trainerWorkloadDeadLetterQueue, deadLetterMessage, propagateTransactionId(transactionId));
    }

    private MessagePostProcessor propagateTransactionId(String transactionId) {
        return jmsMessage -> {
            jmsMessage.setStringProperty(TRANSACTION_ID, transactionId);
            return jmsMessage;
        };
    }

}
