package com.gym.crm.core.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionLoggingFilterTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";

    private TransactionLoggingFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new TransactionLoggingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldPropagateValidTransactionIdFromHeader() throws ServletException, IOException {
        String validTxId = "valid-tx-12345-abcde";
        request.addHeader(TRANSACTION_HEADER, validTxId);
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> mdcValueInsideChain.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        String actual = mdcValueInsideChain.get();
        assertThat(actual).isEqualTo(validTxId);
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(validTxId);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsMissing() throws ServletException, IOException {
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> mdcValueInsideChain.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        String actual = mdcValueInsideChain.get();
        assertThat(actual).isNotNull().isNotBlank();
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(actual);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsInvalid() throws ServletException, IOException {
        String invalidTxId = "invalid_tx_id_with_special_chars!";
        request.addHeader(TRANSACTION_HEADER, invalidTxId);
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> mdcValueInsideChain.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        String actual = mdcValueInsideChain.get();
        assertThat(actual)
                .isNotNull()
                .isNotBlank()
                .isNotEqualTo(invalidTxId);
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(actual);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsTooLong() throws ServletException, IOException {
        String invalidTxId = "a".repeat(51);
        request.addHeader(TRANSACTION_HEADER, invalidTxId);
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> mdcValueInsideChain.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        String actual = mdcValueInsideChain.get();
        assertThat(actual)
                .isNotNull()
                .isNotBlank()
                .isNotEqualTo(invalidTxId);
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(actual);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldCleanUpMdcEvenWhenChainThrowsException() {
        String validTxId = "valid-tx-12345";
        request.addHeader(TRANSACTION_HEADER, validTxId);
        FilterChain filterChain = (req, res) -> {
            throw new RuntimeException("Chain failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chain failure");

        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

}
