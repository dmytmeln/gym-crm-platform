package com.gym.crm.workload.service;

import com.gym.crm.workload.contract.TrainerWorkloadDeadLetterMessage;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadDeadLetterQueuePublisherTest {

    private static final String DEAD_LETTER_QUEUE_NAME = "trainer.workload.updates.dlq";
    private static final String FAILURE_REASON = "username: Username is required";
    private static final String TRANSACTION_ID_VALUE = "valid-tx-12345";

    @Mock
    private JmsTemplate jmsTemplate;

    private TrainerWorkloadDeadLetterQueuePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new TrainerWorkloadDeadLetterQueuePublisher(jmsTemplate, DEAD_LETTER_QUEUE_NAME);
    }

    @Test
    void shouldPublishDeadLetterMessageWithTransactionIdHeader() throws Exception {
        TrainerWorkloadUpdateMessage message = buildMessage();
        Message jmsMessage = mock(Message.class);
        ArgumentCaptor<TrainerWorkloadDeadLetterMessage> deadLetterMessageCaptor = ArgumentCaptor.forClass(TrainerWorkloadDeadLetterMessage.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        publisher.publish(message, FAILURE_REASON, TRANSACTION_ID_VALUE);

        verify(jmsTemplate).convertAndSend(eq(DEAD_LETTER_QUEUE_NAME), deadLetterMessageCaptor.capture(), postProcessorCaptor.capture());
        postProcessorCaptor.getValue().postProcessMessage(jmsMessage);
        assertThat(deadLetterMessageCaptor.getValue().originalMessage()).isEqualTo(message);
        assertThat(deadLetterMessageCaptor.getValue().failureReason()).isEqualTo(FAILURE_REASON);
        assertThat(deadLetterMessageCaptor.getValue().transactionId()).isEqualTo(TRANSACTION_ID_VALUE);
        verify(jmsMessage).setStringProperty(TRANSACTION_ID, TRANSACTION_ID_VALUE);
    }

    private TrainerWorkloadUpdateMessage buildMessage() {
        return TrainerWorkloadUpdateMessage.builder()
                .username("trainer.user")
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(60)
                .actionType(ADD)
                .build();
    }

}
