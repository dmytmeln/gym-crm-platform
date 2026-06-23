package com.gym.crm.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLoggingFilterTest {

    private static final FilterChain READING_BODY_FILTER_CHAIN = (req, res) -> req.getInputStream().readAllBytes();

    private RequestLoggingFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private ListAppender<ILoggingEvent> listAppender;

    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        logger.setLevel(Level.DEBUG);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogRequestStartAndCompletionForGetRequest() throws ServletException, IOException {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/trainees");
        request.setQueryString("name=John");
        response.setStatus(200);
        FilterChain filterChain = (req, res) -> {
            // No body reading needed for GET
        };

        filter.doFilter(request, response, filterChain);

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request started: method=GET, uri=/api/v1/trainees, query=name=John");
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.startsWith("Request completed: method=GET, uri=/api/v1/trainees, status=200, durationMs="));
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .containsOnly(Level.INFO);
    }

    @Test
    void shouldLogSanitizedJsonRequestBodyForPostRequest() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/login");
        request.setContentType("application/json");
        String sensitiveJson = "{\"username\":\"john.doe\",\"password\":\"secret123\"}";
        request.setContent(sensitiveJson.getBytes(UTF_8));

        filter.doFilter(request, response, READING_BODY_FILTER_CHAIN);

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request started: method=POST, uri=/api/v1/auth/login, query=null");
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.startsWith("Request completed: method=POST, uri=/api/v1/auth/login, status=200, durationMs="));
        assertThat(listAppender.list)
                .filteredOn(event -> event.getLevel() == Level.DEBUG)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request body: {\"username\":\"john.doe\",\"password\":\"***\"}");
    }

    @Test
    void shouldLogRequestBodyWithCustomCharset() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/trainers");
        request.setContentType("application/json");
        request.setCharacterEncoding(UTF_16.name());
        String bodyText = "{\"name\":\"Alice\",\"address\":\"12345\"}";
        request.setContent(bodyText.getBytes(UTF_16));

        filter.doFilter(request, response, READING_BODY_FILTER_CHAIN);

        assertThat(listAppender.list)
                .filteredOn(event -> event.getLevel() == Level.DEBUG)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request body: {\"name\":\"Alice\",\"address\":\"1***\"}");
    }

    @Test
    void shouldFallbackToUtf8WhenInvalidCharsetIsProvided() throws ServletException, IOException {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/trainers");
        request.setContentType("application/json");
        request.setCharacterEncoding("invalid-charset-name");
        String bodyText = "{\"name\":\"Bob\"}";
        request.setContent(bodyText.getBytes(UTF_8));

        filter.doFilter(request, response, READING_BODY_FILTER_CHAIN);

        assertThat(listAppender.list)
                .filteredOn(event -> event.getLevel() == Level.DEBUG)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request body: {\"name\":\"Bob\"}");
    }

    @Test
    void shouldLogRequestStartAndCompletionEvenWhenChainThrowsException() {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/error");
        FilterChain filterChain = (req, res) -> {
            throw new RuntimeException("Filter chain exception");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Filter chain exception");

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Request started: method=GET, uri=/api/v1/error, query=null");
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.startsWith("Request completed: method=GET, uri=/api/v1/error, status=200, durationMs="));
    }

}
