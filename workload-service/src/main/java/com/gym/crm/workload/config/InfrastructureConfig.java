package com.gym.crm.workload.config;

import com.gym.crm.jms.EnableGymJms;
import com.gym.crm.logging.EnableTransactionLogging;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableGymJms
@EnableTransactionLogging
@EnableConfigurationProperties(JmsRedeliveryProperties.class)
public class InfrastructureConfig {
}
