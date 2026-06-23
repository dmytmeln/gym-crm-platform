package com.gym.crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_HEADER;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;

public class TransactionLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String transactionHeader = request.getHeader(TRANSACTION_HEADER);
        String transactionId = TransactionContext.resolveTransactionId(transactionHeader);

        MDC.put(TRANSACTION_ID, transactionId);
        response.setHeader(TRANSACTION_HEADER, transactionId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

}
