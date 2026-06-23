package com.gym.crm.workload.exception;

import com.gia.openapi.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.gym.crm.workload.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.workload.exception.ApiError.VALIDATION_ERROR;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String RESPONSE_MESSAGE_TEMPLATE = "%s: %s";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse body = new ErrorResponse(SERVICE_ERROR.getCode(), SERVICE_ERROR.getMessage());
        return ResponseEntity.status(SERVICE_ERROR.getStatus()).body(body);
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
    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                          @NonNull HttpHeaders headers,
                                                                          @NonNull HttpStatusCode status,
                                                                          @NonNull WebRequest request) {
        ErrorResponse body = new ErrorResponse(VALIDATION_ERROR.getCode(), ex.getMessage());

        log.warn("Missing request parameter: {}", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(@NonNull TypeMismatchException ex,
                                                        @NonNull HttpHeaders headers,
                                                        @NonNull HttpStatusCode status,
                                                        @NonNull WebRequest request) {
        ErrorResponse body = new ErrorResponse(VALIDATION_ERROR.getCode(), ex.getMessage());

        log.warn("Request parameter type mismatch: {}", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

}
