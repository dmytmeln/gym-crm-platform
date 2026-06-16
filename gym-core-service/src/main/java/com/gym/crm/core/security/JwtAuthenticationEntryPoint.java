package com.gym.crm.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Unauthorized access attempt to {}: {}", request.getRequestURI(), authException.getMessage());

        response.setStatus(AUTHENTICATION_ERROR.getStatus().value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setHeader(WWW_AUTHENTICATE, "Bearer");

        ErrorResponse errorResponse = new ErrorResponse(AUTHENTICATION_ERROR.getCode(), AUTHENTICATION_ERROR.getMessage());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
