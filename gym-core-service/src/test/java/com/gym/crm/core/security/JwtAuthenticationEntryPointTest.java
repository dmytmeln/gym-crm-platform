package com.gym.crm.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.ErrorResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    @Test
    void shouldReturn401WithBearerChallengeWhenAnonymousUserAccessesProtectedResource() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = new InsufficientAuthenticationException("Full authentication is required");
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        ArgumentCaptor<ErrorResponse> errorCaptor = ArgumentCaptor.forClass(ErrorResponse.class);

        when(request.getRequestURI()).thenReturn("/api/v1/trainees/user");
        when(response.getOutputStream()).thenReturn(outputStream);

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(AUTHENTICATION_ERROR.getStatus().value());
        verify(response).setContentType(APPLICATION_JSON_VALUE);
        verify(response).setHeader(WWW_AUTHENTICATE, "Bearer");
        verify(objectMapper).writeValue(eq(outputStream), errorCaptor.capture());
        ErrorResponse actualError = errorCaptor.getValue();
        assertNotNull(actualError);
        assertEquals(AUTHENTICATION_ERROR.getCode(), actualError.getErrorCode());
        assertEquals(AUTHENTICATION_ERROR.getMessage(), actualError.getErrorMessage());
    }

    @Test
    void shouldReturn401WithBearerChallengeForAnySpringAuthenticationException() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = mock(AuthenticationException.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        ArgumentCaptor<ErrorResponse> errorCaptor = ArgumentCaptor.forClass(ErrorResponse.class);

        when(request.getRequestURI()).thenReturn("/api/v1/trainees/user");
        when(response.getOutputStream()).thenReturn(outputStream);

        entryPoint.commence(request, response, authException);

        verify(response).setStatus(AUTHENTICATION_ERROR.getStatus().value());
        verify(response).setContentType(APPLICATION_JSON_VALUE);
        verify(response).setHeader(WWW_AUTHENTICATE, "Bearer");
        verify(objectMapper).writeValue(eq(outputStream), errorCaptor.capture());
        ErrorResponse actualError = errorCaptor.getValue();
        assertNotNull(actualError);
        assertEquals(AUTHENTICATION_ERROR.getCode(), actualError.getErrorCode());
        assertEquals(AUTHENTICATION_ERROR.getMessage(), actualError.getErrorMessage());
    }

}
