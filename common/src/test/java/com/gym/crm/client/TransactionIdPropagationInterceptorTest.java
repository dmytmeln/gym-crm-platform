package com.gym.crm.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.io.IOException;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_HEADER;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionIdPropagationInterceptorTest {

    private final TransactionIdPropagationInterceptor interceptor = new TransactionIdPropagationInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldPropagateTransactionIdWhenPresentInMdc() throws IOException {
        String transactionId = "valid-tx-12345";
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        HttpHeaders headers = new HttpHeaders();
        byte[] body = new byte[0];
        MDC.put(TRANSACTION_ID, transactionId);

        when(request.getHeaders()).thenReturn(headers);

        interceptor.intercept(request, body, execution);

        assertThat(headers.getFirst(TRANSACTION_HEADER)).isEqualTo(transactionId);
        verify(execution).execute(request, body);
    }

    @Test
    void shouldNotPropagateTransactionIdWhenMissingFromMdc() throws IOException {
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = new byte[0];

        interceptor.intercept(request, body, execution);

        verify(request, never()).getHeaders();
        verify(execution).execute(request, body);
    }

}
