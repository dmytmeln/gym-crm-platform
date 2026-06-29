package com.gym.crm.workload.listener;

import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.contract.WorkloadActionType;
import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.mapper.TrainerWorkloadMapper;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static com.gym.crm.logging.TransactionContext.TRANSACTION_ID;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

    private static final String TRANSACTION_ID_VALUE = "tx-workload-987";

    @Mock
    private TrainerWorkloadService service;

    @Mock
    private TrainerWorkloadMapper mapper;

    @InjectMocks
    private TrainerWorkloadMessageListener listener;

    @Test
    void shouldProcessMessageAndPropagateMdc() {
        TrainerWorkloadUpdateMessage message = TrainerWorkloadUpdateMessage.builder()
                .username("trainer.user")
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, JUNE, 28))
                .trainingDuration(60)
                .actionType(WorkloadActionType.ADD)
                .build();
        TrainerWorkloadUpdate domainUpdate = new TrainerWorkloadUpdate("trainer.user",
                "Liam",
                "Miller",
                true,
                TrainingDate.of(2026, JUNE),
                60,
                ActionType.ADD);

        when(mapper.toDomainUpdate(message)).thenReturn(domainUpdate);
        doAnswer(invocation -> {
            assertThat(MDC.get(TRANSACTION_ID)).isEqualTo(TRANSACTION_ID_VALUE);
            return null;
        }).when(service).updateWorkload(domainUpdate);

        listener.receiveMessage(message, TRANSACTION_ID_VALUE);

        verify(service).updateWorkload(domainUpdate);
        assertThat(MDC.get(TRANSACTION_ID)).isNull();
    }
}
