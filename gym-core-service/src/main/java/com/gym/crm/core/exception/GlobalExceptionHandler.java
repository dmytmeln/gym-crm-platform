package com.gym.crm.core.exception;

import com.gia.openapi.model.ErrorResponse;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gym.crm.core.exception.ApiError.CONFLICT_ERROR;
import static com.gym.crm.core.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.core.exception.ApiError.IP_BLOCKED_ERROR;
import static com.gym.crm.core.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.core.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.core.exception.ApiError.USER_DEACTIVATED_ERROR;
import static com.gym.crm.core.exception.ApiError.VALIDATION_ERROR;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String RESPONSE_MESSAGE_TEMPLATE = "%s: %s";

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        String message = format(RESPONSE_MESSAGE_TEMPLATE, NOT_FOUND_ERROR.getMessage(), ex.getMessage());

        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponse(NOT_FOUND_ERROR, message);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        String message = format(RESPONSE_MESSAGE_TEMPLATE, VALIDATION_ERROR.getMessage(), ex.getMessage());

        log.warn("Validation error: {}", ex.getMessage());
        return buildResponse(VALIDATION_ERROR, message);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        String message = format(RESPONSE_MESSAGE_TEMPLATE, CONFLICT_ERROR.getMessage(), ex.getMessage());

        log.warn("Conflict error: {}", ex.getMessage());
        return buildResponse(CONFLICT_ERROR, message);
    }

    @ExceptionHandler(UserDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleUserDeactivatedException(UserDeactivatedException ex) {
        log.warn("Deactivated user login attempt: {}", ex.getMessage());
        return buildResponse(USER_DEACTIVATED_ERROR);
    }

    @ExceptionHandler(IpBlockedException.class)
    public ResponseEntity<ErrorResponse> handleIpBlockedException(IpBlockedException ex) {
        log.warn("Blocked IP login attempt: {}", ex.getMessage());
        return buildResponse(IP_BLOCKED_ERROR);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(WWW_AUTHENTICATE, "Bearer");

        return buildResponse(AUTHENTICATION_ERROR, httpHeaders);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return buildResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorResponse> handleHibernateException(PersistenceException ex) {
        log.error("Database error occurred", ex);
        return buildResponse(DATABASE_ERROR);
    }

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
            case NOT_FOUND -> NOT_FOUND_ERROR;
            case CONFLICT -> CONFLICT_ERROR;
            default -> SERVICE_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError) {
        return buildResponse(apiError, apiError.getMessage(), EMPTY);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError, HttpHeaders headers) {
        return buildResponse(apiError, apiError.getMessage(), headers);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError, String message) {
        return buildResponse(apiError, message, EMPTY);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ApiError apiError, String message, HttpHeaders headers) {
        ErrorResponse errorResponse = new ErrorResponse(apiError.getCode(), message);
        return ResponseEntity.status(apiError.getStatus()).headers(headers).body(errorResponse);
    }

}
