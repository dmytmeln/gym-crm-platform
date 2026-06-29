package com.gym.crm.workload.config;

import com.gym.crm.jms.EnableGymJms;
import com.gym.crm.logging.EnableTransactionLogging;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableGymJms
@EnableTransactionLogging
public class InfrastructureConfig {
}
