package com.gym.crm.workload;

import com.gym.crm.workload.contract.TrainerWorkloadDeadLetterMessage;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.contract.WorkloadActionType;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadDeadLetterQueueIntegrationTest {

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

    @BeforeEach
    void setUp() {
        jmsTemplate.setReceiveTimeout(QUEUE_DRAIN_TIMEOUT_MS);
        while (jmsTemplate.receive(trainerWorkloadDeadLetterQueue) != null) {
            // Drain leftovers from prior tests.
        }

        jmsTemplate.setReceiveTimeout(DLQ_RECEIVE_TIMEOUT_MS);
    }

    @Test
    void shouldRouteInvalidWorkloadMessageToDeadLetterQueueWithoutUpdatingRepository() {
        String username = "invalid.trainer";
        String transactionId = "valid-tx-12345";
        TrainerWorkloadUpdateMessage message = TrainerWorkloadUpdateMessage.builder()
                .username(username)
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(null)
                .actionType(WorkloadActionType.ADD)
                .build();

        jmsTemplate.convertAndSend(trainerWorkloadQueue, message, jmsMessage -> {
            jmsMessage.setStringProperty("transactionId", transactionId);
            return jmsMessage;
        });

        TrainerWorkloadDeadLetterMessage result = (TrainerWorkloadDeadLetterMessage) jmsTemplate.receiveAndConvert(trainerWorkloadDeadLetterQueue);
        assertThat(result).isNotNull();
        assertThat(result.originalMessage()).isEqualTo(message);
        assertThat(result.failureReason()).isEqualTo("trainingDuration: Training duration is required");
        assertThat(result.transactionId()).isEqualTo(transactionId);
        assertThat(repository.findByUsername(username)).isEmpty();
    }

}
