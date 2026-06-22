package com.gym.crm.core.client;

import com.gym.crm.core.config.ClientConfig;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.security.TokenPropagationInterceptor;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(classes = {ClientConfig.class, WorkloadClientFacade.class, TokenPropagationInterceptor.class}, properties = "app.services.workload.port=${wiremock.server.port}")
@ImportAutoConfiguration({
        RestClientAutoConfiguration.class,
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        AopAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class
})
@ActiveProfiles("test")
@EnableWireMock
class ClientTokenPropagationTest {

    private static final String WORKLOAD_ENDPOINT = "/gym-crm/workload/api/v1/trainer-workloads";
    private static final String BEARER_TOKEN = "Bearer test-jwt-token";

    @Autowired
    private WorkloadClientFacade facade;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest incomingRequest = new MockHttpServletRequest();
        incomingRequest.addHeader(AUTHORIZATION, BEARER_TOKEN);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incomingRequest));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldPropagateAuthorizationHeaderWhenRequestContextIsPresent() {
        Training training = buildTestTraining();

        stubFor(post(urlEqualTo(WORKLOAD_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)));

        facade.addWorkload(training);

        verify(postRequestedFor(urlEqualTo(WORKLOAD_ENDPOINT))
                .withHeader(AUTHORIZATION, equalTo(BEARER_TOKEN)));
    }

    private Training buildTestTraining() {
        User user = User.builder()
                .username("trainer.user")
                .firstName("First")
                .lastName("Last")
                .isActive(true)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, 6, 17))
                .trainingDuration(60)
                .build();
    }

}
