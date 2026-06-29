package com.gym.crm.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jms.redelivery")
public record JmsRedeliveryProperties(
        long initialDelayMs,
        int maximumRedeliveries,
        long maximumDelayMs,
        double backOffMultiplier,
        boolean useExponentialBackOff
) {
}
