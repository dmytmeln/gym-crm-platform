package com.gym.crm.workload;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.exception.TrainerWorkloadListenerErrorHandler;
import com.gym.crm.workload.listener.TrainerWorkloadDeadLetterQueueListener;
import com.gym.crm.workload.listener.TrainerWorkloadMessageListener;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadDeadLetterQueuePublisher;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static ch.qos.logback.classic.Level.ERROR;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadMainQueueFailureIntegrationTest {

    private static final String BROKER_DEAD_LETTER_QUEUE = "ActiveMQ.DLQ";
    private static final long DLQ_RECEIVE_TIMEOUT_MS = 5_000L;
    private static final long QUEUE_DRAIN_TIMEOUT_MS = 200L;

    @Value("${app.jms.queues.trainer-workload}")
    private String trainerWorkloadQueue;

    @Value("${app.jms.queues.trainer-workload-dlq}")
    private String trainerWorkloadDeadLetterQueue;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @MockitoSpyBean
    private TrainerWorkloadMessageListener listener;

    @MockitoSpyBean
    private TrainerWorkloadDeadLetterQueuePublisher deadLetterQueuePublisher;

    private ListAppender<ILoggingEvent> errorHandlerAppender;
    private ListAppender<ILoggingEvent> deadLetterQueueAppender;
    private Logger errorHandlerLogger;
    private Logger deadLetterQueueLogger;

    @BeforeEach
    void setUp() {
        errorHandlerLogger = (Logger) LoggerFactory.getLogger(TrainerWorkloadListenerErrorHandler.class);
        errorHandlerAppender = createAppender(errorHandlerLogger);

        deadLetterQueueLogger = (Logger) LoggerFactory.getLogger(TrainerWorkloadDeadLetterQueueListener.class);
        deadLetterQueueAppender = createAppender(deadLetterQueueLogger);

        jmsTemplate.setReceiveTimeout(QUEUE_DRAIN_TIMEOUT_MS);
        clearQueue(trainerWorkloadDeadLetterQueue);
        clearQueue(BROKER_DEAD_LETTER_QUEUE);

        jmsTemplate.setReceiveTimeout(DLQ_RECEIVE_TIMEOUT_MS);
    }

    @AfterEach
    void tearDown() {
        errorHandlerLogger.detachAppender(errorHandlerAppender);
        deadLetterQueueLogger.detachAppender(deadLetterQueueAppender);
    }

    @Test
    void shouldPublishInvalidWorkloadToApplicationDeadLetterQueueWithoutUpdatingRepository() {
        String username = "invalid.trainer";
        String transactionId = "valid-tx-12345";
        TrainerWorkloadUpdateMessage message = TrainerWorkloadUpdateMessage.builder()
                .username(username)
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(null)
                .actionType(ADD)
                .build();

        jmsTemplate.convertAndSend(trainerWorkloadQueue, message, jmsMessage -> {
            jmsMessage.setStringProperty(TRANSACTION_ID, transactionId);
            return jmsMessage;
        });

        assertThat(jmsTemplate.receive(trainerWorkloadDeadLetterQueue)).isNull();
        assertThat(repository.findByUsername(username)).isEmpty();
        assertThat(deadLetterQueueAppender.list)
                .filteredOn(loggingEvent -> loggingEvent.getLevel() == ERROR)
                .anySatisfy(loggingEvent -> assertThat(loggingEvent.getFormattedMessage())
                        .contains("Consumed workload dead letter message for trainer: " + username)
                        .contains("failureReason: trainingDuration: Training duration is required")
                        .contains("transactionId: " + transactionId));
    }

    @Test
    void shouldRouteInvalidJsonToBrokerDeadLetterQueueWithoutPublishingApplicationDeadLetterMessage() {
        String invalidJson = "{invalid-json";

        jmsTemplate.send(trainerWorkloadQueue, session -> {
            Message jmsMessage = session.createTextMessage(invalidJson);
            jmsMessage.setStringProperty("_type", TrainerWorkloadUpdateMessage.class.getName());
            return jmsMessage;
        });

        assertThat(jmsTemplate.receive(BROKER_DEAD_LETTER_QUEUE)).isNotNull();
        assertThat(jmsTemplate.receive(trainerWorkloadDeadLetterQueue)).isNull();
        assertThat(errorHandlerAppender.list)
                .filteredOn(loggingEvent -> loggingEvent.getLevel() == ERROR)
                .anySatisfy(loggingEvent -> assertThat(loggingEvent.getFormattedMessage())
                        .contains("Failed before trainer workload listener method execution"));
        verify(listener, after(1_000).never()).receiveMessage(any(), any());
    }

    private ListAppender<ILoggingEvent> createAppender(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();

        appender.start();
        logger.addAppender(appender);

        return appender;
    }

    private void clearQueue(String queueName) {
        while (jmsTemplate.receive(queueName) != null) {
            // Drain leftover messages from prior tests.
        }
    }

}
