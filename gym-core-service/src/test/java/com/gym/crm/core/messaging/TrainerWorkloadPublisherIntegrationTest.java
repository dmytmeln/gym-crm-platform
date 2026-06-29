package com.gym.crm.core.messaging;

import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static com.gym.crm.workload.contract.WorkloadActionType.DELETE;
import static java.time.Month.JUNE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadPublisherIntegrationTest {

    private static final String TRANSACTION_ID_VALUE = "valid-tx-12345";
    private static final int RECEIVE_TIMEOUT = 2000;

    @Value("${app.jms.queues.trainer-workload}")
    private String queueName;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TrainerWorkloadPublisher publisher;

    @BeforeEach
    void setUp() {
        MDC.put(TRANSACTION_ID, TRANSACTION_ID_VALUE);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();

        jmsTemplate.setReceiveTimeout(50);
        while (jmsTemplate.receive(queueName) != null) {
        }

        jmsTemplate.setReceiveTimeout(RECEIVE_TIMEOUT);
    }

    @Test
    void shouldPublishJmsMessageOnAddWorkloadAfterCommit() throws Exception {
        Training training = buildTestTraining();

        transactionTemplate.executeWithoutResult(status -> publisher.addWorkload(training));

        Message actual = jmsTemplate.receive(queueName);
        assertNotNull(actual);
        TrainerWorkloadUpdateMessage payload = (TrainerWorkloadUpdateMessage) jmsTemplate.getMessageConverter()
                .fromMessage(actual);
        assertEquals(TRANSACTION_ID_VALUE, actual.getStringProperty(TRANSACTION_ID));
        assertEquals("trainer.user", payload.username());
        assertEquals("First", payload.firstName());
        assertEquals("Last", payload.lastName());
        assertEquals(true, payload.isActive());
        assertEquals(training.getTrainingDate(), payload.trainingDate());
        assertEquals(training.getTrainingDuration(), payload.trainingDuration());
        assertEquals(ADD, payload.actionType());
    }

    @Test
    void shouldPublishJmsMessageOnDeleteWorkloadAfterCommit() throws Exception {
        Training training = buildTestTraining();

        transactionTemplate.executeWithoutResult(status -> publisher.deleteWorkload(training));

        Message actual = jmsTemplate.receive(queueName);
        assertNotNull(actual);
        TrainerWorkloadUpdateMessage payload = (TrainerWorkloadUpdateMessage) jmsTemplate.getMessageConverter()
                .fromMessage(actual);
        assertEquals(TRANSACTION_ID_VALUE, actual.getStringProperty(TRANSACTION_ID));
        assertEquals(DELETE, payload.actionType());
    }

    @Test
    void shouldNotPublishJmsMessageWhenTransactionRollsBack() {
        Training training = buildTestTraining();

        RuntimeException actual = assertThrows(RuntimeException.class, () -> transactionTemplate.executeWithoutResult(status -> {
            publisher.addWorkload(training);
            throw new RuntimeException("Force rollback");
        }));

        assertEquals("Force rollback", actual.getMessage());
        assertNull(jmsTemplate.receive(queueName));
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
                .trainingDate(LocalDate.of(2026, JUNE, 17))
                .trainingDuration(60)
                .build();
    }

}
