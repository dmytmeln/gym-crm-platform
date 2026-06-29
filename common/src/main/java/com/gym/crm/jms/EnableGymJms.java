package com.gym.crm.jms;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(JmsConfig.class)
public @interface EnableGymJms {
}
