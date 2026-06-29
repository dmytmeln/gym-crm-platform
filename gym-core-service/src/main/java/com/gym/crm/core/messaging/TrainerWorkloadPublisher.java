package com.gym.crm.core.messaging;

import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.contract.WorkloadActionType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static com.gym.crm.workload.contract.WorkloadActionType.DELETE;

@Component
@Slf4j
public class TrainerWorkloadPublisher {

    private final JmsTemplate jmsTemplate;
    private final String queueName;

    public TrainerWorkloadPublisher(JmsTemplate jmsTemplate,
                                    @Value("${app.jms.queues.trainer-workload}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    public void addWorkload(Training training) {
        updateWorkload(training, ADD);
    }

    public void deleteWorkload(Training training) {
        updateWorkload(training, DELETE);
    }

    private void updateWorkload(Training training, WorkloadActionType actionType) {
        User user = training.getTrainer().getUser();
        TrainerWorkloadUpdateMessage message = TrainerWorkloadUpdateMessage.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(actionType)
                .build();

        String transactionId = MDC.get(TRANSACTION_ID);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Transaction synchronization is not active");
        }

        log.info("Registering after-commit transaction synchronization for workload update: {} (action: {})",
                message.username(), actionType);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendJmsMessage(message, transactionId);
            }
        });
    }

    private void sendJmsMessage(TrainerWorkloadUpdateMessage message, String transactionId) {
        log.info("Publishing workload update message for trainer: {} (action: {})", message.username(), message.actionType());
        jmsTemplate.convertAndSend(queueName, message, propagateTransactionId(transactionId));
    }

    private MessagePostProcessor propagateTransactionId(String transactionId) {
        return message -> {
            message.setStringProperty(TRANSACTION_ID, transactionId);
            return message;
        };
    }

}
