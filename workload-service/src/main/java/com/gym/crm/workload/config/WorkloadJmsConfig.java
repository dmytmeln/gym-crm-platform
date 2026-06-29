package com.gym.crm.workload.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
public class WorkloadJmsConfig {

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

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                          DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionTransacted(true);

        return factory;
    }

}
