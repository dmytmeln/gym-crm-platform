package com.gym.crm.core.client;

import com.gym.crm.core.config.ClientConfig;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = {ClientConfig.class, WorkloadClientFacade.class}, properties = "app.services.workload.port=${wiremock.server.port}")
@ImportAutoConfiguration({RestClientAutoConfiguration.class, JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ActiveProfiles("test")
@EnableWireMock
class WorkloadClientIntegrationTest {

    private static final String WORKLOAD_ENDPOINT = "/gym-crm/workload/api/v1/trainer-workloads";

    @Autowired
    private WorkloadClientFacade facade;

    @Test
    void shouldSendAddWorkloadRequestToWorkloadService() {
        Training training = buildTestTraining();
        String expectedJson = """
                {
                  "username": "trainer.user",
                  "firstName": "First",
                  "lastName": "Last",
                  "isActive": true,
                  "trainingDate": "2026-06-17",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
                """;

        stubFor(post(urlEqualTo(WORKLOAD_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)));

        facade.addWorkload(training);

        verify(postRequestedFor(urlEqualTo(WORKLOAD_ENDPOINT)).withRequestBody(equalToJson(expectedJson)));
    }

    @Test
    void shouldSendDeleteWorkloadRequestToWorkloadService() {
        Training training = buildTestTraining();
        String expectedJson = """
                {
                  "username": "trainer.user",
                  "firstName": "First",
                  "lastName": "Last",
                  "isActive": true,
                  "trainingDate": "2026-06-17",
                  "trainingDuration": 60,
                  "actionType": "DELETE"
                }
                """;

        stubFor(post(urlEqualTo(WORKLOAD_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(200)));

        facade.deleteWorkload(training);

        verify(postRequestedFor(urlEqualTo(WORKLOAD_ENDPOINT)).withRequestBody(equalToJson(expectedJson)));
    }

    @Test
    void shouldNotPropagateExceptionWhenWorkloadServiceReturnsError() {
        Training training = buildTestTraining();

        stubFor(post(urlEqualTo(WORKLOAD_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(500)));

        assertDoesNotThrow(() -> facade.addWorkload(training));
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
