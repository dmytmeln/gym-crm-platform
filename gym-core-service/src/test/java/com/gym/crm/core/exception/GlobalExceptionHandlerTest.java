package com.gym.crm.core.exception;

import com.gia.openapi.model.ErrorResponse;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static com.gym.crm.core.entity.EntityType.USER;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class GlobalExceptionHandlerTest {

    private static final String EXPECTED_ERROR_MESSAGE_TEMPLATE = "%s: %s";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
    void shouldHandleExceptionInternalWithNotFoundStatus() {
        Exception exception = new Exception("Not found message");

        ResponseEntity<Object> result = handler.handleExceptionInternal(exception, null, new HttpHeaders(), NOT_FOUND, mock(WebRequest.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(result.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(NOT_FOUND_ERROR.getCode());
        assertThat(body.getErrorMessage()).isEqualTo("Not found message");
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

    @Test
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException exception = EntityNotFoundException.forUsername(USER, "username");

        ResponseEntity<ErrorResponse> result = handler.handleEntityNotFoundException(exception);

        assertThat(result.getStatusCode()).isEqualTo(NOT_FOUND_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(NOT_FOUND_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(buildExpectedErrorMessage(NOT_FOUND_ERROR, exception));
    }

    @Test
    void shouldHandleValidationException() {
        ValidationException exception = new ValidationException("Test validation message");

        ResponseEntity<ErrorResponse> result = handler.handleValidationException(exception);

        assertThat(result.getStatusCode()).isEqualTo(VALIDATION_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(VALIDATION_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(buildExpectedErrorMessage(VALIDATION_ERROR, exception));
    }

    @Test
    void shouldHandleAuthenticationException() {
        AuthenticationException exception = new AuthenticationException("Test auth message");

        ResponseEntity<ErrorResponse> result = handler.handleAuthenticationException(exception);

        assertThat(result.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
        assertThat(result.getHeaders()).isEmpty();
    }

    @Test
    void shouldHandleInvalidTokenException() {
        InvalidTokenException exception = new InvalidTokenException("Test invalid token message");

        ResponseEntity<ErrorResponse> result = handler.handleInvalidTokenException(exception);

        assertThat(result.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(AUTHENTICATION_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(AUTHENTICATION_ERROR.getMessage());
        assertThat(result.getHeaders()).containsEntry(WWW_AUTHENTICATE, List.of("Bearer"));
    }

    @Test
    void shouldHandleUserDeactivatedException() {
        UserDeactivatedException exception = new UserDeactivatedException("Test deactivated message");

        ResponseEntity<ErrorResponse> result = handler.handleUserDeactivatedException(exception);

        assertThat(result.getStatusCode()).isEqualTo(USER_DEACTIVATED_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(USER_DEACTIVATED_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(USER_DEACTIVATED_ERROR.getMessage());
    }

    @Test
    void shouldHandleIpBlockedException() {
        IpBlockedException exception = new IpBlockedException("Test blocked message");

        ResponseEntity<ErrorResponse> result = handler.handleIpBlockedException(exception);

        assertThat(result.getStatusCode()).isEqualTo(IP_BLOCKED_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(IP_BLOCKED_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(IP_BLOCKED_ERROR.getMessage());
    }

    @Test
    void shouldHandleHibernateException() {
        PersistenceException exception = new PersistenceException("Test database message");

        ResponseEntity<ErrorResponse> result = handler.handleHibernateException(exception);

        assertThat(result.getStatusCode()).isEqualTo(DATABASE_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(DATABASE_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(DATABASE_ERROR.getMessage());
    }

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
        assertThat(body.getErrorMessage()).isEqualTo(format("%s: fieldName: defaultMessage", VALIDATION_ERROR.getMessage()));
    }

    @Test
    void shouldHandleConflictException() {
        ConflictException exception = new ConflictException("Test conflict message");

        ResponseEntity<ErrorResponse> result = handler.handleConflictException(exception);

        assertThat(result.getStatusCode()).isEqualTo(CONFLICT_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(CONFLICT_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(buildExpectedErrorMessage(CONFLICT_ERROR, exception));
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Test access denied message");

        ResponseEntity<ErrorResponse> result = handler.handleAccessDeniedException(exception);

        assertThat(result.getStatusCode()).isEqualTo(AUTHORIZATION_ERROR.getStatus());
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorCode()).isEqualTo(AUTHORIZATION_ERROR.getCode());
        assertThat(result.getBody().getErrorMessage()).isEqualTo(AUTHORIZATION_ERROR.getMessage());
    }

    private String buildExpectedErrorMessage(ApiError apiError, Exception exception) {
        return format(EXPECTED_ERROR_MESSAGE_TEMPLATE, apiError.getMessage(), exception.getMessage());
    }

}
