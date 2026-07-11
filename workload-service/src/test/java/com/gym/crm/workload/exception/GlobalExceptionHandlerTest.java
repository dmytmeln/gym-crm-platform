package com.gym.crm.workload.exception;

import com.gia.openapi.model.ErrorResponse;
import com.gym.crm.workload.service.common.ValidationErrorFormatter;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static com.gym.crm.workload.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.workload.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.workload.exception.ApiError.VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ValidationErrorFormatter validationErrorFormatter;

    @InjectMocks
    private GlobalExceptionHandler handler;

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
    void shouldHandleMissingServletRequestParameter() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("year", "Integer");

        ResponseEntity<Object> result = handler.handleMissingServletRequestParameter(exception,
                new HttpHeaders(),
                BAD_REQUEST,
                mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void shouldHandleTypeMismatch() {
        TypeMismatchException exception = mock(TypeMismatchException.class);

        when(exception.getMessage()).thenReturn("Method parameter 'month': Failed to convert value");

        ResponseEntity<Object> result = handler.handleTypeMismatch(exception, new HttpHeaders(), BAD_REQUEST, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        when(exception.getMessage()).thenReturn("Validation failed");
        when(validationErrorFormatter.formatViolations(exception)).thenReturn("username: must not be blank");

        ResponseEntity<ErrorResponse> result = handler.handleConstraintViolationException(exception);

        assertThat(result.getStatusCode()).isEqualTo(VALIDATION_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo("Validation error: username: must not be blank");
    }

    @Test
    void shouldHandleDataAccessException() {
        DataAccessException exception = mock(DataAccessException.class);

        ResponseEntity<ErrorResponse> result = handler.handleDatabaseException(exception);

        assertThat(result.getStatusCode()).isEqualTo(DATABASE_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(DATABASE_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(DATABASE_ERROR.getMessage());
    }

}
