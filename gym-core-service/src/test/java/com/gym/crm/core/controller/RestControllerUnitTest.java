package com.gym.crm.core.controller;

import com.gym.crm.core.config.RestControllerTestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@WebMvcTest
@WithMockUser
@Import(RestControllerTestSecurityConfig.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestControllerUnitTest {

    @AliasFor(annotation = WebMvcTest.class, value = "controllers")
    Class<?> value();

}
