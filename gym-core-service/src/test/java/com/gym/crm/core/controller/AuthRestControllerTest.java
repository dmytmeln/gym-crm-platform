package com.gym.crm.core.controller;

import com.gia.openapi.model.ErrorResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.IpBlockedException;
import com.gym.crm.core.exception.ValidationException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.gym.crm.core.entity.EntityType.USER;
import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.core.exception.ApiError.IP_BLOCKED_ERROR;
import static com.gym.crm.core.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.core.exception.ApiError.VALIDATION_ERROR;
import static com.gym.crm.core.helper.JsonUtil.readJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestControllerUnitTest(AuthRestController.class)
class AuthRestControllerTest extends AbstractRestControllerTest {

    private static final String LOGIN_ENDPOINT = AUTH_ENDPOINT + "/login";
    private static final String PASSWORD_ENDPOINT = AUTH_ENDPOINT + "/password";
    private static final String USERNAME = "liam.miller";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword123";

    @Test
    void shouldLoginWhenCredentialsAreValid() throws Exception {
        String requestBody = readJson("json/auth/login_request.json");
        LoginRequest expectedRequest = objectMapper.readValue(requestBody, LoginRequest.class);
        String expectedToken = "mocked-jwt-token";

        when(facade.login(expectedRequest)).thenReturn(expectedToken);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string(AUTHORIZATION, "Bearer " + expectedToken));

        verify(facade).login(expectedRequest);
    }

    @Test
    void shouldFailLoginWhenUsernameIsNull() throws Exception {
        String requestBody = readJson("json/auth/login_invalid_request.json");
        String expectedResponseBody = readJson("json/auth/login_username_null_error.json");

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
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
    void shouldFailLoginWhenPasswordIsNull() throws Exception {
        LoginRequest invalidRequest = buildLoginRequest(null);

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: password: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldReturn404WhenLoginUserNotFound() throws Exception {
        LoginRequest validRequest = buildLoginRequest(PASSWORD);
        EntityNotFoundException exception = EntityNotFoundException.forUsername(USER, USERNAME);

        doThrow(exception).when(facade).login(any());

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
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
    void shouldReturn429WhenLoginIpBlocked() throws Exception {
        LoginRequest validRequest = buildLoginRequest(PASSWORD);
        IpBlockedException exception = new IpBlockedException("IP is temporarily blocked due to too many failed login attempts");

        doThrow(exception).when(facade).login(any());

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isTooManyRequests())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(IP_BLOCKED_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(IP_BLOCKED_ERROR.getMessage());
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccursDuringLogin() throws Exception {
        LoginRequest validRequest = buildLoginRequest(PASSWORD);

        doThrow(new RuntimeException("Unexpected failure")).when(facade).login(any());

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
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
    void shouldReturn500WhenHibernateExceptionOccursDuringLogin() throws Exception {
        LoginRequest validRequest = buildLoginRequest(PASSWORD);

        doThrow(new HibernateException("Database connectivity failure")).when(facade).login(any());

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
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
    void shouldChangePasswordWhenRequestIsValid() throws Exception {
        LoginChangeRequest validRequest = buildLoginChangeRequest(USERNAME, PASSWORD, NEW_PASSWORD);

        mockMvc.perform(put(PASSWORD_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    @Test
    void shouldFailChangePasswordWhenUsernameIsNull() throws Exception {
        LoginChangeRequest invalidRequest = buildLoginChangeRequest(null, PASSWORD, NEW_PASSWORD);

        String actualResponseBody = mockMvc.perform(put(PASSWORD_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: username: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailChangePasswordWhenOldPasswordIsNull() throws Exception {
        LoginChangeRequest invalidRequest = buildLoginChangeRequest(USERNAME, null, NEW_PASSWORD);

        String actualResponseBody = mockMvc.perform(put(PASSWORD_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: oldPassword: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailChangePasswordWhenNewPasswordIsNull() throws Exception {
        LoginChangeRequest invalidRequest = buildLoginChangeRequest(USERNAME, PASSWORD, null);

        String actualResponseBody = mockMvc.perform(put(PASSWORD_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo("Validation error: newPassword: must not be null");
        verifyNoInteractions(facade);
    }

    @Test
    void shouldReturn401WhenAuthenticationFailsDuringPasswordChange() throws Exception {
        LoginChangeRequest validRequest = buildLoginChangeRequest(USERNAME, PASSWORD, NEW_PASSWORD);

        doThrow(new AuthenticationException("User is not authenticated")).when(facade).changePassword(any(LoginChangeRequest.class));

        String actualResponseBody = mockMvc.perform(put(PASSWORD_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    void shouldReturn400WhenValidationExceptionOccursDuringLogin() throws Exception {
        LoginRequest validRequest = buildLoginRequest(PASSWORD);
        ValidationException exception = new ValidationException("Custom validation failed");

        doThrow(exception).when(facade).login(any());

        String actualResponseBody = mockMvc.perform(post(LOGIN_ENDPOINT)
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
    void shouldLogoutWhenTokenIsValid() throws Exception {
        String token = "valid-token";

        mockMvc.perform(post(AUTH_ENDPOINT + "/logout")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        verify(facade).logout(token);
    }

    @Test
    void shouldFailLogoutWhenTokenIsMissing() throws Exception {
        mockMvc.perform(post(AUTH_ENDPOINT + "/logout"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void shouldFailLogoutWhenTokenIsInvalidHeader() throws Exception {
        ValidationException exception = new ValidationException("Invalid authorization header format");

        String actualResponseBody = mockMvc.perform(post(AUTH_ENDPOINT + "/logout")
                        .header(AUTHORIZATION, "Basic abc"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse actualErrorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);
        assertThat(actualErrorResponse.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(actualErrorResponse.getErrorMessage()).isEqualTo(buildExpectedErrorMessage(VALIDATION_ERROR, exception));
        verifyNoInteractions(facade);
    }

    private LoginRequest buildLoginRequest(String password) {
        return new LoginRequest()
                .username(USERNAME)
                .password(password);
    }

    private LoginChangeRequest buildLoginChangeRequest(String username, String oldPassword, String newPassword) {
        return new LoginChangeRequest()
                .username(username)
                .oldPassword(oldPassword)
                .newPassword(newPassword);
    }

}
