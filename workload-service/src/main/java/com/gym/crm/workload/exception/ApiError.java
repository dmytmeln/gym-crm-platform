package com.gym.crm.workload.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@RequiredArgsConstructor
public enum ApiError {

    VALIDATION_ERROR(2760, "Validation error", BAD_REQUEST),
    AUTHENTICATION_ERROR(2805, "Authentication fails", UNAUTHORIZED),
    AUTHORIZATION_ERROR(2806, "User is not authorized for request operation", FORBIDDEN),
    SERVICE_ERROR(3200, "Internal processing error", INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

}
