package com.gym.crm.workload.exception;

import com.gia.openapi.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static com.gym.crm.workload.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.workload.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gym.crm.workload.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.workload.exception.ApiError.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleUnexpectedException() {
        Exception exception = new Exception("Test unexpected message");

        ResponseEntity<ErrorResponse> result = handler.handleUnexpectedException(exception);

        assertThat(result.getStatusCode()).isEqualTo(SERVICE_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(SERVICE_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(SERVICE_ERROR.getMessage());
    }

    @Test
    void shouldHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> result = handler.handleMethodArgumentNotValid(exception, new HttpHeaders(), BAD_REQUEST, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(VALIDATION_ERROR.getStatus());
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Validation error: fieldName: defaultMessage");
    }

    @Test
    void shouldHandleExceptionInternalWithBadRequestStatus() {
        Exception exception = new Exception("Bad request message");

        ResponseEntity<Object> result = handler.handleExceptionInternal(exception, null, new HttpHeaders(), BAD_REQUEST, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Bad request message");
    }

    @Test
    void shouldHandleExceptionInternalWithUnauthorizedStatus() {
        Exception exception = new Exception("Unauthorized message");

        ResponseEntity<Object> result = handler.handleExceptionInternal(exception, null, new HttpHeaders(), UNAUTHORIZED, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Unauthorized message");
    }

    @Test
    void shouldHandleExceptionInternalWithForbiddenStatus() {
        Exception exception = new Exception("Forbidden message");

        ResponseEntity<Object> result = handler.handleExceptionInternal(exception, null, new HttpHeaders(), FORBIDDEN, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(FORBIDDEN);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(AUTHORIZATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Forbidden message");
    }

    @Test
    void shouldHandleExceptionInternalWithOtherStatus() {
        Exception exception = new Exception("Internal server error message");

        ResponseEntity<Object> result = handler.handleExceptionInternal(exception, null, new HttpHeaders(), INTERNAL_SERVER_ERROR, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(SERVICE_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Internal server error message");
    }

}
