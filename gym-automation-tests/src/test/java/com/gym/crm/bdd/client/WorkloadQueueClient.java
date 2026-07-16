package com.gym.crm.bdd.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.bdd.support.GymCoreComponentStack;
import jakarta.jms.Connection;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.bdd.support.GymCoreComponentStack.QUEUE;
import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class WorkloadQueueClient {

    private static final int RECEIVE_POLL_MILLIS = 500;
    private static final int MINIMUM_RECEIVE_TIMEOUT_MILLIS = 1;
    private static final String BROKER_USERNAME = "admin";
    private static final String BROKER_PASSWORD = "admin";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ReceivedWorkload> awaitForUsername(String username, Duration timeout) throws Exception {
        try (Connection connection = createConnection()) {
            connection.start();

            return receiveFromQueue(connection, username, timeout);
        }
    }

    private Connection createConnection() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(GymCoreComponentStack.brokerUrl());
        return connectionFactory.createConnection(BROKER_USERNAME, BROKER_PASSWORD);
    }

    private List<ReceivedWorkload> receiveFromQueue(Connection connection, String username, Duration timeout) throws Exception {
        try (Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
             MessageConsumer consumer = session.createConsumer(session.createQueue(QUEUE))) {
            return pollForUsername(consumer, username, timeout);
        }
    }

    private List<ReceivedWorkload> pollForUsername(MessageConsumer consumer, String username, Duration timeout) throws Exception {
        List<ReceivedWorkload> receivedWorkloads = new ArrayList<>();
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadlineNanos) {
            Message message = consumer.receive(nextReceiveTimeoutMillis(deadlineNanos));
            toReceivedWorkload(message)
                    .filter(workload -> matchesUsername(workload, username))
                    .ifPresent(receivedWorkloads::add);
        }

        return receivedWorkloads;
    }

    private long nextReceiveTimeoutMillis(long deadlineNanos) {
        long remainingMillis = NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
        return Math.clamp(remainingMillis, MINIMUM_RECEIVE_TIMEOUT_MILLIS, RECEIVE_POLL_MILLIS);
    }

    private Optional<ReceivedWorkload> toReceivedWorkload(Message message) throws Exception {
        if (!(message instanceof TextMessage textMessage)) {
            return Optional.empty();
        }

        JsonNode body = objectMapper.readTree(textMessage.getText());
        ReceivedWorkload receivedWorkload = new ReceivedWorkload(body, message.getJMSDestination().toString());

        return Optional.of(receivedWorkload);
    }

    private boolean matchesUsername(ReceivedWorkload workload, String username) {
        String workloadUsername = workload.body().path("username").asText();
        return username.equals(workloadUsername);
    }

    public record ReceivedWorkload(JsonNode body, String destination) {
    }

}
