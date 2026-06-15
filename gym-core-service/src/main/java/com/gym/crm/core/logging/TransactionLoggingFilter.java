package com.gym.crm.core.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(1)
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";
    private static final Pattern SAFE_TX_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]{1,50}$");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String transactionId = request.getHeader(TRANSACTION_HEADER);
        if (!isValidTransactionId(transactionId)) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);
        response.setHeader(TRANSACTION_HEADER, transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private boolean isValidTransactionId(String transactionId) {
        return transactionId != null && SAFE_TX_ID_PATTERN.matcher(transactionId).matches();
    }

}