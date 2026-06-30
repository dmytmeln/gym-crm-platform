package com.gym.crm.workload.config;

import com.gym.crm.workload.exception.TrainerWorkloadListenerErrorHandler;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import java.util.List;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;

@Configuration
public class WorkloadJmsConfig {

    private static final long DLQ_INITIAL_REDELIVERY_DELAY_MS = 0L;
    private static final int DLQ_MAXIMUM_REDELIVERIES = 0;
    private static final long DLQ_MAXIMUM_REDELIVERY_DELAY_MS = 0L;
    private static final double DLQ_BACK_OFF_MULTIPLIER = 1.0;
    private static final boolean DLQ_USE_EXPONENTIAL_MULTIPLIER = false;
    private static final List<String> TRUSTED_PACKAGES = List.of("java.lang", "java.util", "com.gym.crm");

    @Bean
    public ActiveMQConnectionFactoryCustomizer activeMqConnectionFactoryCustomizer(JmsRedeliveryProperties jmsRedeliveryProperties) {
        return connectionFactory -> {
            RedeliveryPolicy redeliveryPolicy = connectionFactory.getRedeliveryPolicy();

            redeliveryPolicy.setInitialRedeliveryDelay(jmsRedeliveryProperties.initialDelayMs());
            redeliveryPolicy.setMaximumRedeliveries(jmsRedeliveryProperties.maximumRedeliveries());
            redeliveryPolicy.setMaximumRedeliveryDelay(jmsRedeliveryProperties.maximumDelayMs());
            redeliveryPolicy.setBackOffMultiplier(jmsRedeliveryProperties.backOffMultiplier());
            redeliveryPolicy.setUseExponentialBackOff(jmsRedeliveryProperties.useExponentialBackOff());
        };
    }

    @Bean("connectionFactory")
    @Primary
    public ActiveMQConnectionFactory connectionFactory(@Value("${spring.activemq.broker-url}")
                                                       String brokerUrl,
                                                       @Value("${spring.activemq.user:}")
                                                       String username,
                                                       @Value("${spring.activemq.password:}")
                                                       String password,
                                                       ObjectProvider<ActiveMQConnectionFactoryCustomizer> customizers) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        connectionFactory.setTrustAllPackages(false);
        connectionFactory.setTrustedPackages(TRUSTED_PACKAGES);

        customizers.orderedStream().forEach(customizer -> customizer.customize(connectionFactory));

        return connectionFactory;
    }

    @Bean("trainerWorkloadDeadLetterQueueConnectionFactory")
    public ActiveMQConnectionFactory trainerWorkloadDeadLetterQueueConnectionFactory(@Value("${spring.activemq.broker-url}")
                                                                                     String brokerUrl,
                                                                                     @Value("${spring.activemq.user:}")
                                                                                     String username,
                                                                                     @Value("${spring.activemq.password:}")
                                                                                     String password) {
        ActiveMQConnectionFactory deadLetterQueueConnectionFactory = new ActiveMQConnectionFactory();
        deadLetterQueueConnectionFactory.setBrokerURL(brokerUrl);
        deadLetterQueueConnectionFactory.setUserName(username);
        deadLetterQueueConnectionFactory.setPassword(password);
        deadLetterQueueConnectionFactory.setTrustAllPackages(false);
        deadLetterQueueConnectionFactory.setTrustedPackages(TRUSTED_PACKAGES);

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setInitialRedeliveryDelay(DLQ_INITIAL_REDELIVERY_DELAY_MS);
        redeliveryPolicy.setMaximumRedeliveries(DLQ_MAXIMUM_REDELIVERIES);
        redeliveryPolicy.setMaximumRedeliveryDelay(DLQ_MAXIMUM_REDELIVERY_DELAY_MS);
        redeliveryPolicy.setBackOffMultiplier(DLQ_BACK_OFF_MULTIPLIER);
        redeliveryPolicy.setUseExponentialBackOff(DLQ_USE_EXPONENTIAL_MULTIPLIER);
        deadLetterQueueConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);

        return deadLetterQueueConnectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("connectionFactory")
                                                                          ConnectionFactory connectionFactory,
                                                                          DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                                          TrainerWorkloadListenerErrorHandler trainerWorkloadListenerErrorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionTransacted(true);
        factory.setErrorHandler(trainerWorkloadListenerErrorHandler);

        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory trainerWorkloadDeadLetterQueueListenerContainerFactory(@Qualifier("trainerWorkloadDeadLetterQueueConnectionFactory")
                                                                                                     ConnectionFactory connectionFactory,
                                                                                                     DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionAcknowledgeMode(AUTO_ACKNOWLEDGE);
        factory.setSessionTransacted(false);

        return factory;
    }

}
