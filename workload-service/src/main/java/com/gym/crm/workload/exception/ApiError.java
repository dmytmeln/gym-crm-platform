package com.gym.crm.workload.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@RequiredArgsConstructor
public enum ApiError {

    VALIDATION_ERROR(2760, "Validation error", BAD_REQUEST),
    SERVICE_ERROR(3200, "Internal processing error", INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

}
