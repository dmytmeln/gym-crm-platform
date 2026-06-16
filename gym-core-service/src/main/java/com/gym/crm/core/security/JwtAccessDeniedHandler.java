package com.gym.crm.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.gym.crm.core.exception.ApiError.AUTHORIZATION_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(AUTHORIZATION_ERROR.getStatus().value());
        response.setContentType(APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(AUTHORIZATION_ERROR.getCode(), AUTHORIZATION_ERROR.getMessage());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
