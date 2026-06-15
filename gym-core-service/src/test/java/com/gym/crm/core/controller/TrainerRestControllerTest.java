package com.gym.crm.core.controller;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.ErrorResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.ConflictException;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.ValidationException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.WithAnonymousUser;

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.core.entity.EntityType.TRAINER;
import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiError.CONFLICT_ERROR;
import static com.gym.crm.core.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.core.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.core.exception.ApiError.VALIDATION_ERROR;
import static com.gym.crm.core.helper.JsonUtil.readJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestControllerUnitTest(TrainerRestController.class)
class TrainerRestControllerTest extends AbstractRestControllerTest {

    private static final String TRAINER_RESOURCE_ENDPOINT = TRAINERS_ENDPOINT + "/{username}";
    private static final String REGISTER_ENDPOINT = TRAINERS_ENDPOINT + "/register";
    private static final String ACTIVATION_ENDPOINT = TRAINER_RESOURCE_ENDPOINT + "/activation";
    private static final String TRAINER_TRAININGS_ENDPOINT = TRAINER_RESOURCE_ENDPOINT + "/trainings";
    private static final String USERNAME = "liam.miller";
    private static final String FIRST_NAME = "Liam";
    private static final String LAST_NAME = "Miller";
    private static final String SPECIALIZATION = "Yoga";

    @Test
    @WithAnonymousUser
    void shouldRegisterTrainerWhenRequestIsValid() throws Exception {
        String requestBody = readJson("json/trainer/register_request.json");
        String expectedResponseBody = readJson("json/trainer/register_response.json");
        TrainerCreateRequest expectedRequest = objectMapper.readValue(requestBody, TrainerCreateRequest.class);
        TrainerCreateResponse mockResponse = objectMapper.readValue(expectedResponseBody, TrainerCreateResponse.class);

        when(facade.createTrainer(any(TrainerCreateRequest.class))).thenReturn(mockResponse);

        String actualResponseBody = mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponseBody, actualResponseBody, true);
        verify(facade).createTrainer(expectedRequest);
    }

    @Test
    void shouldFailRegisterTrainerWhenFirstNameIsNull() throws Exception {
        TrainerCreateRequest invalidRequest = buildTrainerCreateRequest(null, LAST_NAME, SPECIALIZATION);

        String actualResponseBody = mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: firstName: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailRegisterTrainerWhenLastNameIsNull() throws Exception {
        TrainerCreateRequest invalidRequest = buildTrainerCreateRequest(FIRST_NAME, null, SPECIALIZATION);

        String actualResponseBody = mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: lastName: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailRegisterTrainerWhenSpecializationIsNull() throws Exception {
        TrainerCreateRequest invalidRequest = buildTrainerCreateRequest(FIRST_NAME, LAST_NAME, null);

        String actualResponseBody = mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: specialization: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldGetTrainerProfileWhenUsernameIsValid() throws Exception {
        String expectedResponseBody = readJson("json/trainer/get_profile_response.json");
        TrainerGetResponse mockResponse = objectMapper.readValue(expectedResponseBody, TrainerGetResponse.class);

        when(facade.getTrainerByUsername(USERNAME)).thenReturn(mockResponse);

        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponseBody, actualResponseBody, true);
        verify(facade).getTrainerByUsername(USERNAME);
    }

    @Test
    void shouldReturn404WhenTrainerNotFound() throws Exception {
        EntityNotFoundException exception = EntityNotFoundException.forUsername(TRAINER, USERNAME);

        doThrow(exception).when(facade).getTrainerByUsername(USERNAME);

        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(NOT_FOUND_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(buildExpectedErrorMessage(NOT_FOUND_ERROR, exception));
    }

    @Test
    void shouldReturn401WhenAuthenticationFailsOnGetTrainerProfile() throws Exception {
        doThrow(new AuthenticationException("User is not authenticated")).when(facade).getTrainerByUsername(USERNAME);

        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccursOnGetTrainerProfile() throws Exception {
        doThrow(new RuntimeException("Unexpected failure")).when(facade).getTrainerByUsername(USERNAME);

        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(SERVICE_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(SERVICE_ERROR.getMessage());
    }

    @Test
    void shouldReturn500WhenHibernateExceptionOccursOnGetTrainerProfile() throws Exception {
        doThrow(new HibernateException("Database connectivity failure")).when(facade).getTrainerByUsername(USERNAME);

        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(DATABASE_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(DATABASE_ERROR.getMessage());
    }

    @Test
    void shouldGetTrainerTrainingsWithAllFilters() throws Exception {
        GetTrainerTrainingResponse trainingResponse = new GetTrainerTrainingResponse()
                .trainingName("Morning Yoga")
                .trainingType("Yoga")
                .traineeName("billy.herrington")
                .trainingDate(LocalDate.of(2025, 7, 20))
                .trainingDuration(55);

        when(facade.getTrainerTrainings(any())).thenReturn(List.of(trainingResponse));

        String actualResponseBody = mockMvc.perform(get(TRAINER_TRAININGS_ENDPOINT, USERNAME)
                        .param("fromDate", "2025-07-01")
                        .param("toDate", "2025-07-31")
                        .param("traineeName", "billy.herrington"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<GetTrainerTrainingResponse> actualResponse = objectMapper.readValue(actualResponseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GetTrainerTrainingResponse.class));
        assertThat(actualResponse).hasSize(1);
        assertThat(actualResponse.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(actualResponse.getFirst().getTrainingType()).isEqualTo("Yoga");
        assertThat(actualResponse.getFirst().getTraineeName()).isEqualTo("billy.herrington");
        verify(facade).getTrainerTrainings(any());
    }

    @Test
    void shouldGetTrainerTrainingsWithNoOptionalFilters() throws Exception {
        when(facade.getTrainerTrainings(any())).thenReturn(List.of());

        mockMvc.perform(get(TRAINER_TRAININGS_ENDPOINT, USERNAME))
                .andExpect(status().isOk());

        verify(facade).getTrainerTrainings(any());
    }

    @Test
    void shouldUpdateTrainerProfileWhenRequestIsValid() throws Exception {
        TrainerUpdateRequest validRequest = buildTrainerUpdateRequest(FIRST_NAME, LAST_NAME);
        TrainerUpdateResponse mockResponse = new TrainerUpdateResponse()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .specialization(SPECIALIZATION)
                .isActive(true);

        when(facade.updateTrainer(eq(USERNAME), any(TrainerUpdateRequest.class))).thenReturn(mockResponse);

        String actualResponseBody = mockMvc.perform(put(TRAINER_RESOURCE_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TrainerUpdateResponse actualResponse = objectMapper.readValue(actualResponseBody, TrainerUpdateResponse.class);
        assertThat(actualResponse.getUsername()).isEqualTo(USERNAME);
        assertThat(actualResponse.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actualResponse.getIsActive()).isTrue();
        verify(facade).updateTrainer(eq(USERNAME), any(TrainerUpdateRequest.class));
    }

    @Test
    void shouldFailUpdateTrainerProfileWhenFirstNameIsNull() throws Exception {
        TrainerUpdateRequest invalidRequest = buildTrainerUpdateRequest(null, LAST_NAME);

        String actualResponseBody = mockMvc.perform(put(TRAINER_RESOURCE_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: firstName: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailUpdateTrainerProfileWhenLastNameIsNull() throws Exception {
        TrainerUpdateRequest invalidRequest = buildTrainerUpdateRequest(FIRST_NAME, null);

        String actualResponseBody = mockMvc.perform(put(TRAINER_RESOURCE_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: lastName: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailUpdateTrainerProfileWhenIsActiveIsNull() throws Exception {
        TrainerUpdateRequest invalidRequest = new TrainerUpdateRequest();
        invalidRequest.firstName(FIRST_NAME);
        invalidRequest.lastName(LAST_NAME);

        String actualResponseBody = mockMvc.perform(put(TRAINER_RESOURCE_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: isActive: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldChangeTrainerActivationStatusWhenRequestIsValid() throws Exception {
        ActivationStatusRequest validRequest = new ActivationStatusRequest(true);

        mockMvc.perform(patch(ACTIVATION_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(facade).updateTrainerActivationStatus(USERNAME, true);
    }

    @Test
    void shouldFailChangeTrainerActivationStatusWhenIsActiveIsNull() throws Exception {
        ActivationStatusRequest invalidRequest = new ActivationStatusRequest(null);

        String actualResponseBody = mockMvc.perform(patch(ACTIVATION_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: isActive: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldReturn409WhenChangingTrainerActivationStatusToAlreadySameStatus() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest(true);
        ConflictException exception = new ConflictException("Trainer with username: username is already active");

        doThrow(exception).when(facade).updateTrainerActivationStatus(USERNAME, true);

        String actualResponseBody = mockMvc.perform(patch(ACTIVATION_ENDPOINT, USERNAME)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(CONFLICT_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Conflict error: Trainer with username: username is already active");
        verify(facade).updateTrainerActivationStatus(USERNAME, true);
    }

    @Test
    void shouldReturn400WhenValidationExceptionOccursDuringRegistration() throws Exception {
        TrainerCreateRequest validRequest = buildTrainerCreateRequest(FIRST_NAME, LAST_NAME, SPECIALIZATION);
        ValidationException exception = new ValidationException("Custom validation failed");

        doThrow(exception).when(facade).createTrainer(any(TrainerCreateRequest.class));

        String actualResponseBody = mockMvc.perform(post(REGISTER_ENDPOINT)
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
    void shouldReturn401WhenGetTrainerProfileAnonymous() throws Exception {
        String actualResponseBody = mockMvc.perform(get(TRAINER_RESOURCE_ENDPOINT, USERNAME))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
    }

    private TrainerCreateRequest buildTrainerCreateRequest(String firstName, String lastName, String specialization) {
        return new TrainerCreateRequest()
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization);
    }

    private TrainerUpdateRequest buildTrainerUpdateRequest(String firstName, String lastName) {
        return new TrainerUpdateRequest()
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true);
    }

}
