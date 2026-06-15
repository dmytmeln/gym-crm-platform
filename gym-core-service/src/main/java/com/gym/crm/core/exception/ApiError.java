package com.gym.crm.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
public enum ApiError {

    VALIDATION_ERROR(2760, "Validation error", BAD_REQUEST),
    AUTHENTICATION_ERROR(2805, "Authentication fails", UNAUTHORIZED),
    AUTHORIZATION_ERROR(2806, "User is not authorized for request operation", FORBIDDEN),
    USER_DEACTIVATED_ERROR(2807, "User account is deactivated", FORBIDDEN),
    IP_BLOCKED_ERROR(2808, "IP is temporarily blocked due to too many failed login attempts", TOO_MANY_REQUESTS),
    NOT_FOUND_ERROR(2835, "Requested data was not found", NOT_FOUND),
    CONFLICT_ERROR(2809, "Conflict error", CONFLICT),
    SERVICE_ERROR(3200, "Internal processing error", INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(3358, "Database error", INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ApiError(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
