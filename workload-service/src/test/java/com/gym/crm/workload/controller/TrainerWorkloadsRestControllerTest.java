package com.gym.crm.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum;
import com.gym.crm.logging.LoggingConfig;
import com.gym.crm.logging.TransactionContext;
import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.mapper.TrainerWorkloadMapper;
import com.gym.crm.workload.service.TrainerWorkloadService;
import com.gym.crm.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.gym.crm.workload.security.SecurityConfig;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.gym.crm.workload.exception.ApiError.VALIDATION_ERROR;
import static java.time.Month.JULY;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadsRestController.class)
@Import({SecurityConfig.class, LoggingConfig.class})
@WithMockUser
class TrainerWorkloadsRestControllerTest {

    private static final String TRAINER_WORKLOADS_ENDPOINT = "/api/v1/trainer-workloads";
    private static final String USERNAME = "marcus.stone";
    private static final String FIRST_NAME = "Marcus";
    private static final String LAST_NAME = "Stone";

    @MockitoBean
    private TrainerWorkloadService service;

    @MockitoBean
    private TrainerWorkloadMapper mapper;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateTrainerWorkloadWhenRequestIsValid() throws Exception {
        TrainerWorkloadUpdateRequest request = buildRequest(USERNAME);
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();

        when(mapper.toDomainUpdate(any(TrainerWorkloadUpdateRequest.class))).thenReturn(domainUpdate);

        mockMvc.perform(post(TRAINER_WORKLOADS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service).updateWorkload(domainUpdate);
    }

    @Test
    void shouldFailUpdateTrainerWorkloadWhenUsernameIsEmpty() throws Exception {
        String usernamePattern = "\"^[a-zA-Z]+\\.[a-zA-Z]+(\\d+)?$\"";
        TrainerWorkloadUpdateRequest request = buildRequest("");

        mockMvc.perform(post(TRAINER_WORKLOADS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Validation error: username: must match " + usernamePattern));
    }

    @Test
    void shouldGetTrainerWorkloadWhenRequestIsValid() throws Exception {
        int trainerWorkingHours = 120;

        when(service.getWorkingHours(any(TrainerWorkloadSearchFilter.class))).thenReturn(trainerWorkingHours);

        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("year", "2025")
                        .param("month", JULY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingWorkingHours").value(trainerWorkingHours));
    }

    @Test
    void shouldFailGetTrainerWorkloadWhenYearIsMissing() throws Exception {
        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("month", JULY.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Required request parameter 'year' for method parameter type Integer is not present"));
    }

    @Test
    void shouldFailGetTrainerWorkloadWhenMonthIsInvalid() throws Exception {
        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("year", "2025")
                        .param("month", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage", startsWith("Method parameter 'month': Failed to convert value of type 'java.lang.String' to required type 'java.time.Month';")));
    }

    @Test
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenRequestIsAnonymous() throws Exception {
        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("year", "2025")
                        .param("month", JULY.name()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPropagateIncomingTransactionIdToResponse() throws Exception {
        String transactionId = "valid-tx-12345";

        when(service.getWorkingHours(any(TrainerWorkloadSearchFilter.class))).thenReturn(120);

        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("year", "2025")
                        .param("month", JULY.name())
                        .header(TransactionContext.TRANSACTION_HEADER, transactionId))
                .andExpect(status().isOk())
                .andExpect(header().string(TransactionContext.TRANSACTION_HEADER, transactionId));
    }

    @Test
    void shouldGenerateTransactionIdWhenRequestHeaderMissing() throws Exception {
        when(service.getWorkingHours(any(TrainerWorkloadSearchFilter.class))).thenReturn(120);

        mockMvc.perform(get(TRAINER_WORKLOADS_ENDPOINT + "/{username}", USERNAME)
                        .param("year", "2025")
                        .param("month", JULY.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists(TransactionContext.TRANSACTION_HEADER));
    }

    private TrainerWorkloadUpdateRequest buildRequest(String username) {
        return new TrainerWorkloadUpdateRequest(username,
                FIRST_NAME,
                LAST_NAME,
                true,
                LocalDate.of(2025, JULY, 20),
                60,
                ActionTypeEnum.ADD);
    }

    private TrainerWorkloadUpdate buildDomainUpdate() {
        return TrainerWorkloadUpdate.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .trainingDate(TrainingDate.of(2025, JULY))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();
    }

}
