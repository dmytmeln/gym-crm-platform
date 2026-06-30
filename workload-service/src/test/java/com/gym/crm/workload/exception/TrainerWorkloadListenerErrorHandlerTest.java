package com.gym.crm.workload.exception;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.adapter.ListenerExecutionFailedException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.ErrorHandler;

import static ch.qos.logback.classic.Level.ERROR;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadListenerErrorHandlerTest {

    private static final String TRANSACTION_ID_VALUE = "tx-workload-987";

    private final ErrorHandler errorHandler = new TrainerWorkloadListenerErrorHandler();

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(TrainerWorkloadListenerErrorHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogContextWhenHandlingWrappedProcessingException() {
        RuntimeException cause = new RuntimeException("processing failed");
        String username = "trainer.user";
        String expectedLogMessage = "Failed to process workload update message for trainer: %s (action: %s, transactionId: %s)"
                .formatted(username, ADD, TRANSACTION_ID_VALUE);
        TrainerWorkloadProcessingException exception = new TrainerWorkloadProcessingException(TRANSACTION_ID_VALUE, username, ADD, cause);
        ListenerExecutionFailedException listenerException = new ListenerExecutionFailedException("listener failed", exception);

        errorHandler.handleError(listenerException);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.getFirst();
        assertThat(loggingEvent.getLevel()).isEqualTo(ERROR);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
        assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("Failed to process trainer workload message");
    }

    @Test
    void shouldLogGenericFailureWhenHandlingPreMethodException() {
        MessageConversionException exception = new MessageConversionException("invalid json");
        String listenerErrorMsg = "listener failed";
        ListenerExecutionFailedException listenerException = new ListenerExecutionFailedException(listenerErrorMsg, exception);
        String expectedLogMessage = "Failed before trainer workload listener method execution";

        errorHandler.handleError(listenerException);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.getFirst();
        assertThat(loggingEvent.getLevel()).isEqualTo(ERROR);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
        assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo(listenerErrorMsg);
    }

}
