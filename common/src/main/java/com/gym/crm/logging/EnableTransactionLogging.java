package com.gym.crm.logging;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(LoggingConfig.class)
public @interface EnableTransactionLogging {
}
