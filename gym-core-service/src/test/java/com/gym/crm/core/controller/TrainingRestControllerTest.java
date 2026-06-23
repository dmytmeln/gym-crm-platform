package com.gym.crm.core.controller;

import com.gia.openapi.model.ErrorResponse;
import com.gym.crm.core.exception.DownstreamConnectionException;
import com.gym.crm.core.exception.DownstreamTimeoutException;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.ValidationException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.core.entity.EntityType.TRAINEE;
import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.core.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_TIMEOUT_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_UNAVAILABLE_ERROR;
import static com.gym.crm.core.exception.ApiError.VALIDATION_ERROR;
import static com.gym.crm.core.helper.JsonUtil.readJson;
import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestControllerUnitTest(TrainingRestController.class)
class TrainingRestControllerTest extends AbstractRestControllerTest {

    private static final String TRAININGS_TYPES_ENDPOINT = TRAININGS_ENDPOINT + "/types";
    private static final String TRAINEE_USERNAME = "billy.herrington";
    private static final String TRAINER_USERNAME = "ricardo.milos";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2025, JULY, 20);
    private static final Integer TRAINING_DURATION = 55;

    @Test
    void shouldAddTrainingWhenRequestIsValid() throws Exception {
        String requestBody = readJson("json/training/add_training_request.json");
        TrainingCreateRequest expectedRequest = objectMapper.readValue(requestBody, TrainingCreateRequest.class);

        mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(facade).createTraining(expectedRequest);
    }

    @Test
    void shouldFailAddTrainingWhenTrainingNameIsNull() throws Exception {
        String requestBody = readJson("json/training/add_invalid_request.json");
        String expectedResponseBody = readJson("json/training/add_training_name_null_error.json");

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponseBody, actualResponseBody, true);
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailAddTrainingWhenTrainingDateIsNull() throws Exception {
        TrainingCreateRequest invalidRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, null, TRAINING_DURATION);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: trainingDate: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailAddTrainingWhenTrainingDurationIsNull() throws Exception {
        TrainingCreateRequest invalidRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, null);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: trainingDuration: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailAddTrainingWhenTraineeUsernameIsNull() throws Exception {
        TrainingCreateRequest invalidRequest = buildTrainingCreateRequest(null, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: traineeUsername: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailAddTrainingWhenTrainerUsernameIsNull() throws Exception {
        TrainingCreateRequest invalidRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, null, TRAINING_DATE, TRAINING_DURATION);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: trainerUsername: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailAddTrainingWhenTrainingDurationIsZero() throws Exception {
        TrainingCreateRequest invalidRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, 0);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: trainingDuration: must be greater than or equal to 1");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldReturn404WhenTraineeOrTrainerNotFoundOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);
        EntityNotFoundException exception = EntityNotFoundException.forUsername(TRAINEE, TRAINEE_USERNAME);

        doThrow(exception).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(NOT_FOUND_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(buildExpectedErrorMessage(NOT_FOUND_ERROR, exception));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccursOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);

        doThrow(new RuntimeException("Unexpected failure")).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(SERVICE_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(SERVICE_ERROR.getMessage());
    }

    @Test
    void shouldReturn500WhenHibernateExceptionOccursOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);

        doThrow(new HibernateException("Database connectivity failure")).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(DATABASE_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(DATABASE_ERROR.getMessage());
    }

    @Test
    void shouldReturn504WhenWorkloadServiceTimesOutOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);
        DownstreamTimeoutException exception = new DownstreamTimeoutException("workload-service", 3, new RuntimeException("timeout"));

        doThrow(exception).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isGatewayTimeout())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(SERVICE_TIMEOUT_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Timeout: workload-service did not respond within 3s");
    }

    @Test
    void shouldReturn503WhenWorkloadServiceConnectionFailsOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);
        DownstreamConnectionException exception = new DownstreamConnectionException("workload-service", new RuntimeException("connect"));

        doThrow(exception).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isServiceUnavailable())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(SERVICE_UNAVAILABLE_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Connection error: Cannot connect to workload-service");
    }

    @Test
    void shouldGetTrainingTypes() throws Exception {
        TrainingTypeResponse cardio = new TrainingTypeResponse()
                .id(1)
                .name("Cardio");
        TrainingTypeResponse yoga = new TrainingTypeResponse()
                .id(2)
                .name("Yoga");

        when(facade.getAllTrainingTypes()).thenReturn(List.of(cardio, yoga));

        String actualResponseBody = mockMvc.perform(get(TRAININGS_TYPES_ENDPOINT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<TrainingTypeResponse> actualResponse = objectMapper.readValue(actualResponseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingTypeResponse.class));
        assertThat(actualResponse).hasSize(2);
        assertThat(actualResponse.get(0).getName()).isEqualTo("Cardio");
        assertThat(actualResponse.get(1).getName()).isEqualTo("Yoga");
        verify(facade).getAllTrainingTypes();
    }

    @Test
    void shouldGetEmptyTrainingTypesList() throws Exception {
        when(facade.getAllTrainingTypes()).thenReturn(List.of());

        String actualResponseBody = mockMvc.perform(get(TRAININGS_TYPES_ENDPOINT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<TrainingTypeResponse> actualResponse = objectMapper.readValue(actualResponseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingTypeResponse.class));
        assertThat(actualResponse).isEmpty();
        verify(facade).getAllTrainingTypes();
    }

    @Test
    void shouldReturn400WhenValidationExceptionOccursOnAddTraining() throws Exception {
        TrainingCreateRequest validRequest = buildTrainingCreateRequest(TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_DATE, TRAINING_DURATION);
        ValidationException exception = new ValidationException("Custom validation failed");

        doThrow(exception).when(facade).createTraining(validRequest);

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(buildExpectedErrorMessage(VALIDATION_ERROR, exception));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenAddTrainingAnonymous() throws Exception {
        String requestBody = readJson("json/training/add_training_request.json");

        String actualResponseBody = mockMvc.perform(post(TRAININGS_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    @WithAnonymousUser
    void shouldReturn401WhenGetTrainingTypesAnonymous() throws Exception {
        String actualResponseBody = mockMvc.perform(get(TRAININGS_TYPES_ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldDeleteTrainingWhenRequestIsValid() throws Exception {
        Long id = 42L;

        mockMvc.perform(delete(TRAININGS_ENDPOINT + "/{id}", id))
                .andExpect(status().isOk());

        verify(facade).deleteTraining(id, "marcus.stone");
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldReturn404WhenTrainingNotFoundOnDeleteTraining() throws Exception {
        Long id = 42L;
        EntityNotFoundException exception = new EntityNotFoundException("Training not found with ID: " + id);
        doThrow(exception).when(facade).deleteTraining(id, "marcus.stone");

        String actualResponseBody = mockMvc.perform(delete(TRAININGS_ENDPOINT + "/{id}", id))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(NOT_FOUND_ERROR.getCode());
    }

    private TrainingCreateRequest buildTrainingCreateRequest(String traineeUsername,
                                                             String trainerUsername,
                                                             LocalDate trainingDate,
                                                             Integer trainingDuration) {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername(traineeUsername);
        request.setTrainerUsername(trainerUsername);
        request.setTrainingName(TRAINING_NAME);
        request.setTrainingDate(trainingDate);
        request.setTrainingDuration(trainingDuration);

        return request;
    }

}
