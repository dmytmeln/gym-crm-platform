package com.gym.crm.workload;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.gym.crm.workload.contract.TrainerWorkloadDeadLetterMessage;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.listener.TrainerWorkloadDeadLetterQueueListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static ch.qos.logback.classic.Level.ERROR;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.DELETE;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadDeadLetterQueueListenerIntegrationTest {

    private static final String BROKER_DEAD_LETTER_QUEUE = "ActiveMQ.DLQ";
    private static final long DLQ_RECEIVE_TIMEOUT_MS = 5_000L;
    private static final long QUEUE_DRAIN_TIMEOUT_MS = 200L;

    @Value("${app.jms.queues.trainer-workload-dlq}")
    private String trainerWorkloadDeadLetterQueue;

    @Autowired
    private JmsTemplate jmsTemplate;

    private ListAppender<ILoggingEvent> deadLetterQueueAppender;
    private Logger deadLetterQueueLogger;

    @BeforeEach
    void setUp() {
        deadLetterQueueLogger = (Logger) LoggerFactory.getLogger(TrainerWorkloadDeadLetterQueueListener.class);
        deadLetterQueueAppender = new ListAppender<>();
        deadLetterQueueAppender.start();
        deadLetterQueueLogger.addAppender(deadLetterQueueAppender);

        jmsTemplate.setReceiveTimeout(QUEUE_DRAIN_TIMEOUT_MS);
        clearQueue(trainerWorkloadDeadLetterQueue);
        clearQueue(BROKER_DEAD_LETTER_QUEUE);

        jmsTemplate.setReceiveTimeout(DLQ_RECEIVE_TIMEOUT_MS);
    }

    @AfterEach
    void tearDown() {
        deadLetterQueueLogger.detachAppender(deadLetterQueueAppender);
    }

    @Test
    void shouldAcknowledgeApplicationDeadLetterMessageWithoutForwardingToBrokerDeadLetterQueue() {
        String transactionId = "dlq-tx-54321";
        TrainerWorkloadDeadLetterMessage message = createDeadLetterMessage(transactionId);

        jmsTemplate.convertAndSend(trainerWorkloadDeadLetterQueue, message, jmsMessage -> {
            jmsMessage.setStringProperty(TRANSACTION_ID, transactionId);
            return jmsMessage;
        });

        assertThat(jmsTemplate.receive(trainerWorkloadDeadLetterQueue)).isNull();
        assertThat(jmsTemplate.receive(BROKER_DEAD_LETTER_QUEUE)).isNull();
        assertThat(deadLetterQueueAppender.list)
                .filteredOn(loggingEvent -> loggingEvent.getLevel() == ERROR)
                .anySatisfy(loggingEvent -> assertThat(loggingEvent.getFormattedMessage())
                        .contains("Consumed workload dead letter message for trainer: dlq.ack.trainer")
                        .contains("failureReason: downstream storage failure")
                        .contains("transactionId: " + transactionId));
    }

    private void clearQueue(String queueName) {
        while (jmsTemplate.receive(queueName) != null) {
            // Drain leftover messages from prior tests.
        }
    }

    private TrainerWorkloadDeadLetterMessage createDeadLetterMessage(String transactionId) {
        TrainerWorkloadUpdateMessage originalMessage = TrainerWorkloadUpdateMessage.builder()
                .username("dlq.ack.trainer")
                .firstName("Nina")
                .lastName("Cole")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(45)
                .actionType(DELETE)
                .build();

        return TrainerWorkloadDeadLetterMessage.builder()
                .originalMessage(originalMessage)
                .failureReason("downstream storage failure")
                .transactionId(transactionId)
                .build();
    }

}
