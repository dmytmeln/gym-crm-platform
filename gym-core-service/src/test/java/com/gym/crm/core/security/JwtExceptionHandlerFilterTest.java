package com.gym.crm.core.security;

import com.gym.crm.core.exception.InvalidTokenException;
import com.gym.crm.core.exception.UserDeactivatedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class JwtExceptionHandlerFilterTest {

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @InjectMocks
    private JwtExceptionHandlerFilter filter;

    @Test
    void shouldContinueFilterChainWhenNoExceptionThrown() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(handlerExceptionResolver);
    }

    @Test
    void shouldTranslateAndDelegateToHandlerExceptionResolverWhenDisabledExceptionThrown() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        DisabledException exception = new DisabledException("Disabled");

        doThrow(exception).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), captor.capture());
        Exception actual = captor.getValue();
        assertInstanceOf(UserDeactivatedException.class, actual);
        assertEquals("User account is deactivated", actual.getMessage());
        assertEquals(exception, actual.getCause());
    }

    @Test
    void shouldTranslateAndDelegateToHandlerExceptionResolverWhenAuthenticationExceptionThrown() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        doThrow(exception).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), captor.capture());
        Exception actual = captor.getValue();
        assertInstanceOf(InvalidTokenException.class, actual);
        assertEquals("Invalid token", actual.getMessage());
        assertEquals(exception, actual.getCause());
    }

}
