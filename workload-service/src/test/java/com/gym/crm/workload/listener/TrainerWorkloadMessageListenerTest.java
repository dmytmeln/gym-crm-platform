package com.gym.crm.workload.listener;

import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.exception.TrainerWorkloadProcessingException;
import com.gym.crm.workload.mapper.TrainerWorkloadMapper;
import com.gym.crm.workload.service.TrainerWorkloadDeadLetterQueuePublisher;
import com.gym.crm.workload.service.TrainerWorkloadMessageValidator;
import com.gym.crm.workload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jms.JmsException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static com.gym.crm.workload.contract.WorkloadActionType.ADD;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

    private static final String TRANSACTION_ID_VALUE = "tx-workload-987";
    private static final String TRAINER_USERNAME = "trainer.user";
    private static final String VALIDATION_FAILURE_REASON = "username: Username is required";
    private static final String INVALID_TRANSACTION_ID = "invalid transaction id";
    private static final String GENERATED_TRANSACTION_ID_PATTERN = "^[a-f0-9\\-]{36}$";

    @Mock
    private TrainerWorkloadService service;

    @Mock
    private TrainerWorkloadMapper mapper;

    @Mock
    private TrainerWorkloadDeadLetterQueuePublisher deadLetterQueuePublisher;

    @Mock
    private TrainerWorkloadMessageValidator messageValidator;

    @InjectMocks
    private TrainerWorkloadMessageListener listener;

    @Test
    void shouldProcessValidMessageAndPropagateMdc() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();

        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        when(messageValidator.validate(message)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            assertThat(MDC.get(TRANSACTION_ID)).isEqualTo(TRANSACTION_ID_VALUE);
            return null;
        }).when(service).updateWorkload(domainUpdate);

        listener.receiveMessage(message, TRANSACTION_ID_VALUE);

        verify(service).updateWorkload(domainUpdate);
        verify(deadLetterQueuePublisher, never()).publish(message, VALIDATION_FAILURE_REASON, TRANSACTION_ID_VALUE);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldSendInvalidMessageToDeadLetterQueueAndSkipBusinessProcessing() {
        TrainerWorkloadUpdateMessage message = buildMessage();

        when(messageValidator.validate(message)).thenReturn(Optional.of(VALIDATION_FAILURE_REASON));

        listener.receiveMessage(message, TRANSACTION_ID_VALUE);

        verify(deadLetterQueuePublisher).publish(message, VALIDATION_FAILURE_REASON, TRANSACTION_ID_VALUE);
        verifyNoInteractions(mapper);
        verifyNoInteractions(service);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldResolveTransactionIdBeforePublishingDeadLetterMessage() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        ArgumentCaptor<String> transactionIdCaptor = ArgumentCaptor.forClass(String.class);

        when(messageValidator.validate(message)).thenReturn(Optional.of(VALIDATION_FAILURE_REASON));

        listener.receiveMessage(message, INVALID_TRANSACTION_ID);

        verify(deadLetterQueuePublisher).publish(eq(message), eq(VALIDATION_FAILURE_REASON), transactionIdCaptor.capture());
        assertThat(transactionIdCaptor.getValue())
                .isNotEqualTo(INVALID_TRANSACTION_ID)
                .matches(GENERATED_TRANSACTION_ID_PATTERN);
        verifyNoInteractions(mapper);
        verifyNoInteractions(service);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldWrapDeadLetterPublishingFailureAndClearMdc() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        JmsException expected = new JmsException("dlq send failed") {};

        when(messageValidator.validate(message)).thenReturn(Optional.of(VALIDATION_FAILURE_REASON));
        doAnswer(invocation -> {
            assertThat(MDC.get(TRANSACTION_ID)).isEqualTo(TRANSACTION_ID_VALUE);
            throw expected;
        }).when(deadLetterQueuePublisher).publish(message, VALIDATION_FAILURE_REASON, TRANSACTION_ID_VALUE);

        Throwable actualThrowable = catchThrowable(() -> listener.receiveMessage(message, TRANSACTION_ID_VALUE));

        assertThat(actualThrowable).isInstanceOf(TrainerWorkloadProcessingException.class);
        TrainerWorkloadProcessingException actual = (TrainerWorkloadProcessingException) actualThrowable;
        assertThat(actual.getTransactionId()).isEqualTo(TRANSACTION_ID_VALUE);
        assertThat(actual.getTrainerUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(actual.getActionType()).isEqualTo(ADD);
        assertThat(actual.getCause()).isSameAs(expected);
        verify(deadLetterQueuePublisher).publish(message, VALIDATION_FAILURE_REASON, TRANSACTION_ID_VALUE);
        verifyNoInteractions(mapper);
        verifyNoInteractions(service);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldPropagateBusinessProcessingFailureAndClearMdc() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();
        RuntimeException expected = new RuntimeException("processing failed");

        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        when(messageValidator.validate(message)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            assertThat(MDC.get(TRANSACTION_ID)).isEqualTo(TRANSACTION_ID_VALUE);
            throw expected;
        }).when(service).updateWorkload(domainUpdate);

        Throwable actualThrowable = catchThrowable(() -> listener.receiveMessage(message, TRANSACTION_ID_VALUE));

        assertThat(actualThrowable).isInstanceOf(TrainerWorkloadProcessingException.class);
        TrainerWorkloadProcessingException actual = (TrainerWorkloadProcessingException) actualThrowable;
        assertThat(actual.getTransactionId()).isEqualTo(TRANSACTION_ID_VALUE);
        assertThat(actual.getTrainerUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(actual.getActionType()).isEqualTo(ADD);
        assertThat(actual.getCause()).isSameAs(expected);
        verify(service).updateWorkload(domainUpdate);
        verify(deadLetterQueuePublisher, never()).publish(message, VALIDATION_FAILURE_REASON, TRANSACTION_ID_VALUE);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldSendNonRecoverableConstraintViolationExceptionToDeadLetterQueue() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();
        ConstraintViolationException mockException = mock(ConstraintViolationException.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("username");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        when(mockException.getConstraintViolations()).thenReturn(Set.of(violation));
        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        when(messageValidator.validate(message)).thenReturn(Optional.empty());
        doThrow(mockException).when(service).updateWorkload(domainUpdate);

        listener.receiveMessage(message, TRANSACTION_ID_VALUE);

        verify(service).updateWorkload(domainUpdate);
        verify(deadLetterQueuePublisher).publish(message, "username: must not be blank", TRANSACTION_ID_VALUE);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldSendNonRecoverableDataAccessExceptionToDeadLetterQueue() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();
        NonTransientDataAccessException mockException = mock(NonTransientDataAccessException.class);

        when(mockException.getMessage()).thenReturn("non-transient database error");
        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        when(messageValidator.validate(message)).thenReturn(Optional.empty());
        doThrow(mockException).when(service).updateWorkload(domainUpdate);

        listener.receiveMessage(message, TRANSACTION_ID_VALUE);

        verify(service).updateWorkload(domainUpdate);
        verify(deadLetterQueuePublisher).publish(message, "non-transient database error", TRANSACTION_ID_VALUE);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    @Test
    void shouldPropagateRecoverableDataAccessException() {
        TrainerWorkloadUpdateMessage message = buildMessage();
        TrainerWorkloadUpdate domainUpdate = buildDomainUpdate();
        TransientDataAccessException mockException = mock(TransientDataAccessException.class);

        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        when(messageValidator.validate(message)).thenReturn(Optional.empty());
        doThrow(mockException).when(service).updateWorkload(domainUpdate);

        Throwable actualThrowable = catchThrowable(() -> listener.receiveMessage(message, TRANSACTION_ID_VALUE));

        assertThat(actualThrowable).isInstanceOf(TrainerWorkloadProcessingException.class);
        TrainerWorkloadProcessingException actual = (TrainerWorkloadProcessingException) actualThrowable;
        assertThat(actual.getCause()).isSameAs(mockException);
        verify(service).updateWorkload(domainUpdate);
        verify(deadLetterQueuePublisher, never()).publish(eq(message), any(), eq(TRANSACTION_ID_VALUE));
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }

    private TrainerWorkloadUpdateMessage buildMessage() {
        return TrainerWorkloadUpdateMessage.builder()
                .username(TRAINER_USERNAME)
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(60)
                .actionType(ADD)
                .build();
    }

    private TrainerWorkloadUpdate buildDomainUpdate() {
        return TrainerWorkloadUpdate.builder()
                .username(TRAINER_USERNAME)
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(TrainingDate.of(2026, JUNE))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();
    }

}
