package com.gym.crm.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotAuthenticateWhenJwtTokenNotPresent() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenHeaderIsNotBearer() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationManager);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateWhenBearerTokenIsPresent() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        Authentication authenticatedResult = mock(Authentication.class);
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer validJwtToken");
        when(authenticationManager.authenticate(authCaptor.capture())).thenReturn(authenticatedResult);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication capturedAuth = authCaptor.getValue();
        assertNotNull(capturedAuth);
        assertInstanceOf(JwtTokenAuthentication.class, capturedAuth);
        assertEquals("validJwtToken", ((JwtTokenAuthentication) capturedAuth).getToken());
        assertEquals(authenticatedResult, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPropagateExceptionWhenAuthenticationFails() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer invalidToken");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(exception);

        assertThrows(AuthenticationException.class, () -> filter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
