package com.gym.crm.bdd.workload.client;

import com.gym.crm.bdd.support.CucumberObjectMapper;
import com.gym.crm.bdd.workload.model.WorkloadUpdatePayload;
import com.gym.crm.bdd.workload.support.WorkloadComponentStack;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

import static com.gym.crm.bdd.workload.support.WorkloadComponentStack.QUEUE;
import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;

public class TrainerWorkloadQueuePublisher {

    private static final String BROKER_USERNAME = "admin";
    private static final String BROKER_PASSWORD = "admin";
    private static final String MESSAGE_TYPE_PROPERTY = "_type";
    private static final String MESSAGE_TYPE = "com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage";

    public void publishUpdate(WorkloadUpdatePayload payload) throws Exception {
        try (Connection connection = createConnection();
             Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
             MessageProducer producer = createProducer(session)) {
            Message message = createMessage(session, payload);
            producer.send(message);
        }
    }

    private Connection createConnection() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(WorkloadComponentStack.brokerUrl());
        return connectionFactory.createConnection(BROKER_USERNAME, BROKER_PASSWORD);
    }

    private MessageProducer createProducer(Session session) throws JMSException {
        Queue queue = session.createQueue(QUEUE);
        return session.createProducer(queue);
    }

    private Message createMessage(Session session, WorkloadUpdatePayload payload) throws Exception {
        String json = CucumberObjectMapper.instance().writeValueAsString(payload);

        Message message = session.createTextMessage(json);
        message.setStringProperty(MESSAGE_TYPE_PROPERTY, MESSAGE_TYPE);

        return message;
    }

}
