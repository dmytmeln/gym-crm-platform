package com.gym.crm.client;

import com.gym.crm.logging.TransactionContext;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Optional;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_HEADER;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;

public class TransactionIdPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                        @NonNull byte[] body,
                                        @NonNull ClientHttpRequestExecution execution) throws IOException {
        getTransactionId().ifPresent(transactionId -> request.getHeaders().add(TRANSACTION_HEADER, transactionId));

        return execution.execute(request, body);
    }

    private Optional<String> getTransactionId() {
        return Optional.ofNullable(MDC.get(TRANSACTION_ID))
                .filter(TransactionContext::isValidTransactionId);
    }

}
