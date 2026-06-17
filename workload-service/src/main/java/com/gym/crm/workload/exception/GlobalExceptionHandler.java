package com.gym.crm.workload.exception;

import com.gia.openapi.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.gym.crm.workload.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.workload.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gym.crm.workload.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.workload.exception.ApiError.VALIDATION_ERROR;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.EMPTY;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String RESPONSE_MESSAGE_TEMPLATE = "%s: %s";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildResponse(SERVICE_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> format(RESPONSE_MESSAGE_TEMPLATE, fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(joining(", "));

        String message = format(RESPONSE_MESSAGE_TEMPLATE, VALIDATION_ERROR.getMessage(), violations);
        ErrorResponse body = new ErrorResponse(VALIDATION_ERROR.getCode(), message);

        log.warn("Request body validation failed: {}", ex.getMessage());
        return ResponseEntity.status(VALIDATION_ERROR.getStatus()).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                             @Nullable Object body,
                                                             @NonNull HttpHeaders headers,
                                                             @NonNull HttpStatusCode statusCode,
                                                             @NonNull WebRequest request) {
        ApiError apiError = getApiErrorForStatus(statusCode);
        ErrorResponse errorResponse = new ErrorResponse(apiError.getCode(), ex.getMessage());

        log.warn("Spring MVC exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(statusCode).headers(headers).body(errorResponse);
    }

    private ApiError getApiErrorForStatus(HttpStatusCode status) {
        if (!(status instanceof HttpStatus httpStatus)) {
            return SERVICE_ERROR;
        }

        return switch (httpStatus) {
            case BAD_REQUEST -> VALIDATION_ERROR;
            case UNAUTHORIZED -> AUTHENTICATION_ERROR;
            case FORBIDDEN -> AUTHORIZATION_ERROR;
            default -> SERVICE_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError) {
        return buildResponse(apiError, apiError.getMessage(), EMPTY);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError, String message, HttpHeaders headers) {
        ErrorResponse errorResponse = new ErrorResponse(apiError.getCode(), message);
        return ResponseEntity.status(apiError.getStatus()).headers(headers).body(errorResponse);
    }

}
