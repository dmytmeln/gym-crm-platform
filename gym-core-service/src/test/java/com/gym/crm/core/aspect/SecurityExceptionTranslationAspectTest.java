package com.gym.crm.core.aspect;

import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.UserDeactivatedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityExceptionTranslationAspectTest {

    @InjectMocks
    private SecurityExceptionTranslationAspect aspect;

    @Test
    void shouldProceedWhenNoExceptionIsThrownInLogin() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object expectedResult = new Object();

        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object actual = aspect.translateLoginExceptions(joinPoint);

        assertEquals(expectedResult, actual);
        verify(joinPoint).proceed();
    }

    @Test
    void shouldTranslateDisabledExceptionToUserDeactivatedExceptionInLogin() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenThrow(new DisabledException("Disabled"));

        UserDeactivatedException exception = assertThrows(UserDeactivatedException.class, () -> aspect.translateLoginExceptions(joinPoint));

        assertEquals("User account is deactivated", exception.getMessage());
    }

    @Test
    void shouldTranslateBadCredentialsExceptionToAuthenticationExceptionInLogin() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> aspect.translateLoginExceptions(joinPoint));

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void shouldTranslateUsernameNotFoundExceptionToAuthenticationExceptionInLogin() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenThrow(new UsernameNotFoundException("User not found"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> aspect.translateLoginExceptions(joinPoint));

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void shouldTranslateInsufficientAuthenticationExceptionToAuthenticationExceptionInLogin() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenThrow(new InsufficientAuthenticationException("Insufficient auth"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> aspect.translateLoginExceptions(joinPoint));

        assertEquals("Invalid username or password", exception.getMessage());
    }

}
