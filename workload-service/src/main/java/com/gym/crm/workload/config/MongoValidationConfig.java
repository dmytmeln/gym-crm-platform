package com.gym.crm.workload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class MongoValidationConfig {

    @Bean
    public ValidatingEntityCallback validatingEntityCallback(LocalValidatorFactoryBean factory) {
        return new ValidatingEntityCallback(factory);
    }

}
