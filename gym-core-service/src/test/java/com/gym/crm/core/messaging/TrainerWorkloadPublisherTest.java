package com.gym.crm.core.messaging;

import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadPublisherTest {

    private static final String QUEUE_NAME = "trainer-workload-queue";
    private static final String TRANSACTION_ID_VALUE = "valid-tx-12345";
    private static final String UPDATED_TRANSACTION_ID_VALUE = "updated-tx-67890";

    @Mock
    private JmsTemplate jmsTemplate;

    private TrainerWorkloadPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new TrainerWorkloadPublisher(jmsTemplate, QUEUE_NAME);

        MDC.put(TRANSACTION_ID, TRANSACTION_ID_VALUE);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void shouldThrowExceptionWhenTransactionSynchronizationIsNotActive() {
        Training training = buildTestTraining();

        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> publisher.addWorkload(training));

        assertEquals("Transaction synchronization is not active", actual.getMessage());
        verify(jmsTemplate, never()).convertAndSend(anyString(), any(), any(MessagePostProcessor.class));
    }

    @Test
    void shouldRegisterSynchronizationWhenTransactionSynchronizationIsActive() {
        Training training = buildTestTraining();
        TransactionSynchronizationManager.initSynchronization();

        publisher.addWorkload(training);

        assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
        assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
        verify(jmsTemplate, never()).convertAndSend(anyString(), any(), any(MessagePostProcessor.class));
    }

    @Test
    void shouldSendJmsMessageWithCapturedTransactionIdAfterCommit() throws Exception {
        Training training = buildTestTraining();
        Message message = mock(Message.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        TransactionSynchronization synchronization = registerAddWorkloadSynchronization(training);
        MDC.put(TRANSACTION_ID, UPDATED_TRANSACTION_ID_VALUE);

        synchronization.afterCommit();

        verify(jmsTemplate).convertAndSend(eq(QUEUE_NAME), any(), postProcessorCaptor.capture());
        postProcessorCaptor.getValue().postProcessMessage(message);
        verify(message).setStringProperty(TRANSACTION_ID, TRANSACTION_ID_VALUE);
    }

    private TransactionSynchronization registerAddWorkloadSynchronization(Training training) {
        TransactionSynchronizationManager.initSynchronization();
        publisher.addWorkload(training);

        return TransactionSynchronizationManager.getSynchronizations().getFirst();
    }

    private Training buildTestTraining() {
        User user = User.builder()
                .username("trainer.user")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, 6, 17))
                .trainingDuration(60)
                .build();
    }

}
