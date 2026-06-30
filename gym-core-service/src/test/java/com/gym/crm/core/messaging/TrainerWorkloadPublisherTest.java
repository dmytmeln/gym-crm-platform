package com.gym.crm.core.messaging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
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
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadPublisherTest {

    private static final String TRAINER_USERNAME = "trainer.user";
    private static final String QUEUE_NAME = "trainer-workload-queue";
    private static final String TRANSACTION_ID_VALUE = "valid-tx-12345";
    private static final String UPDATED_TRANSACTION_ID_VALUE = "updated-tx-67890";

    @Mock
    private JmsTemplate jmsTemplate;

    private TrainerWorkloadPublisher publisher;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        publisher = new TrainerWorkloadPublisher(jmsTemplate, QUEUE_NAME);

        logger = (Logger) LoggerFactory.getLogger(TrainerWorkloadPublisher.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        MDC.put(TRANSACTION_ID, TRANSACTION_ID_VALUE);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        logger.detachAppender(listAppender);

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

    @Test
    void shouldLogJmsFailureWithoutPropagatingFromAfterCommitCallback() {
        Training training = buildTestTraining();
        JmsException expected = new JmsException("send failed") {};
        String expectedErrorMessage = "Failed to publish workload update message for trainer: %s (action: %s, queue: %s, transactionId: %s)"
                .formatted(TRAINER_USERNAME, ADD, QUEUE_NAME, TRANSACTION_ID_VALUE);
        TransactionSynchronization synchronization = registerAddWorkloadSynchronization(training);

        doThrow(expected).when(jmsTemplate).convertAndSend(eq(QUEUE_NAME), any(), any(MessagePostProcessor.class));

        assertDoesNotThrow(synchronization::afterCommit);

        verify(jmsTemplate).convertAndSend(eq(QUEUE_NAME), any(), any(MessagePostProcessor.class));
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
                .containsExactly(tuple(INFO, "Registering after-commit transaction synchronization for workload update: %s (action: %s)"
                                .formatted(TRAINER_USERNAME, ADD)),
                        tuple(INFO, "Publishing workload update message for trainer: %s (action: %s)".formatted(TRAINER_USERNAME, ADD)),
                        tuple(ERROR, expectedErrorMessage));
        ILoggingEvent loggingEvent = listAppender.list.getLast();
        assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("send failed");
    }

    private TransactionSynchronization registerAddWorkloadSynchronization(Training training) {
        TransactionSynchronizationManager.initSynchronization();
        publisher.addWorkload(training);

        return TransactionSynchronizationManager.getSynchronizations().getFirst();
    }

    private Training buildTestTraining() {
        User user = User.builder()
                .username(TRAINER_USERNAME)
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, JUNE, 17))
                .trainingDuration(60)
                .build();
    }

}
