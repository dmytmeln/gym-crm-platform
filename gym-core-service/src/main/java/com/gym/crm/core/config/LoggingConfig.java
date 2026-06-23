package com.gym.crm.core.config;

import com.gym.crm.logging.RequestLoggingFilter;
import com.gym.crm.logging.TransactionLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<TransactionLoggingFilter> transactionLoggingFilter() {
        FilterRegistrationBean<TransactionLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TransactionLoggingFilter());
        registration.setOrder(1);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.setOrder(2);

        return registration;
    }

}
