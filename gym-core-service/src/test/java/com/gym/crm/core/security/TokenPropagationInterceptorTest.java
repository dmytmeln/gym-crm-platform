package com.gym.crm.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class TokenPropagationInterceptorTest {

    private static final String BEARER_TOKEN = "Bearer test-jwt-token";

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private TokenPropagationInterceptor interceptor;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldPropagateTokenWhenAuthHeaderIsPresent() throws IOException {
        byte[] body = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        RequestContextHolder.setRequestAttributes(requestAttributes);

        when(requestAttributes.getRequest()).thenReturn(servletRequest);
        when(servletRequest.getHeader(AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(request.getHeaders()).thenReturn(headers);

        interceptor.intercept(request, body, execution);

        assertEquals(BEARER_TOKEN, headers.getFirst(AUTHORIZATION));
        verify(execution).execute(request, body);
    }

    @Test
    void shouldNotPropagateTokenWhenAuthHeaderIsMissing() throws IOException {
        byte[] body = new byte[0];
        RequestContextHolder.setRequestAttributes(requestAttributes);

        when(requestAttributes.getRequest()).thenReturn(servletRequest);
        when(servletRequest.getHeader(AUTHORIZATION)).thenReturn(null);

        interceptor.intercept(request, body, execution);

        verify(request, never()).getHeaders();
        verify(execution).execute(request, body);
    }

    @Test
    void shouldNotPropagateTokenWhenRequestAttributesAreMissing() throws IOException {
        byte[] body = new byte[0];

        interceptor.intercept(request, body, execution);

        verify(request, never()).getHeaders();
        verify(execution).execute(request, body);
    }

}
