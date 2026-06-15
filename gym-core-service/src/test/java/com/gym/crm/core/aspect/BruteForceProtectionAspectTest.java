package com.gym.crm.core.aspect;

import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.IpBlockedException;
import com.gym.crm.core.exception.UserDeactivatedException;
import com.gym.crm.core.security.LoginAttemptService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionAspectTest {

    private static final String TEST_IP_ADDRESS = "192.168.1.100";

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private BruteForceProtectionAspect aspect;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(TEST_IP_ADDRESS);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldProceedWhenIpNotBlockedAndLoginSucceeds() throws Throwable {
        Object expectedResult = "jwt-token";

        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = aspect.protectAgainstBruteForce(joinPoint);

        assertEquals(expectedResult, result);
        verify(loginAttemptService).loginSucceeded(TEST_IP_ADDRESS);
        verify(loginAttemptService, never()).loginFailed(TEST_IP_ADDRESS);
    }

    @Test
    void shouldThrowIpBlockedExceptionWhenIpBlocked() throws Throwable {
        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(true);

        assertThrows(IpBlockedException.class, () -> aspect.protectAgainstBruteForce(joinPoint));

        verify(joinPoint, never()).proceed();
        verify(loginAttemptService, never()).loginSucceeded(TEST_IP_ADDRESS);
        verify(loginAttemptService, never()).loginFailed(TEST_IP_ADDRESS);
    }

    @Test
    void shouldIncrementFailedAttemptsWhenLoginFailsWithAuthenticationException() throws Throwable {
        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(joinPoint.proceed()).thenThrow(new AuthenticationException("Invalid username or password"));

        assertThrows(AuthenticationException.class, () -> aspect.protectAgainstBruteForce(joinPoint));

        verify(loginAttemptService).loginFailed(TEST_IP_ADDRESS);
        verify(loginAttemptService, never()).loginSucceeded(TEST_IP_ADDRESS);
    }

    @Test
    void shouldIncrementFailedAttemptsWhenLoginFailsWithUserDeactivatedException() throws Throwable {
        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(joinPoint.proceed()).thenThrow(new UserDeactivatedException("Deactivated"));

        assertThrows(UserDeactivatedException.class, () -> aspect.protectAgainstBruteForce(joinPoint));

        verify(loginAttemptService).loginFailed(TEST_IP_ADDRESS);
        verify(loginAttemptService, never()).loginSucceeded(TEST_IP_ADDRESS);
    }

    @Test
    void shouldThrowIllegalStateWhenNoActiveRequest() throws Throwable {
        RequestContextHolder.resetRequestAttributes();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> aspect.protectAgainstBruteForce(joinPoint));

        assertEquals("No request attributes bound to the thread", exception.getMessage());
        verify(joinPoint, never()).proceed();
        verify(loginAttemptService, never()).isBlocked(anyString());
        verify(loginAttemptService, never()).loginSucceeded(anyString());
        verify(loginAttemptService, never()).loginFailed(anyString());
    }

    @Test
    void shouldThrowIllegalStateWhenRequestIsNotServletRequest() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        RequestAttributes requestAttributes = mock(RequestAttributes.class);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        when(requestAttributes.resolveReference(REFERENCE_REQUEST)).thenReturn(mock(Object.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> aspect.protectAgainstBruteForce(joinPoint));

        assertEquals("Request attributes do not contain a ServletRequest", exception.getMessage());
        verify(joinPoint, never()).proceed();
        verify(loginAttemptService, never()).isBlocked(anyString());
        verify(loginAttemptService, never()).loginSucceeded(anyString());
        verify(loginAttemptService, never()).loginFailed(anyString());
    }

}
