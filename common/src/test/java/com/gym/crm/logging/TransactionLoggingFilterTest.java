package com.gym.crm.logging;

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

import static com.gym.crm.logging.TransactionContext.TRANSACTION_HEADER;
import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionLoggingFilterTest {

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
        AtomicReference<String> actual = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> actual.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        assertThat(actual.get()).isEqualTo(validTxId);
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(validTxId);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsMissing() throws ServletException, IOException {
        AtomicReference<String> actual = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> actual.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        assertThat(actual.get()).isNotNull().isNotBlank();
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(actual.get());
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsInvalid() throws ServletException, IOException {
        request.addHeader(TRANSACTION_HEADER, "invalid_tx_id_with_special_chars!");
        AtomicReference<String> actual = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> actual.set(MDC.get(TRANSACTION_ID));

        filter.doFilter(request, response, filterChain);

        assertThat(actual.get())
                .isNotNull()
                .isNotBlank()
                .isNotEqualTo("invalid_tx_id_with_special_chars!");
        assertThat(response.getHeader(TRANSACTION_HEADER)).isEqualTo(actual.get());
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldCleanUpMdcEvenWhenChainThrowsException() {
        request.addHeader(TRANSACTION_HEADER, "valid-tx-12345");
        FilterChain filterChain = (req, res) -> {
            throw new RuntimeException("Chain failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chain failure");

        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

}
