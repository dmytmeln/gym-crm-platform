package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.workload.dto.ActionType.ADD;
import static com.gym.crm.workload.dto.ActionType.DELETE;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    private static final String USERNAME = "marcus.stone";

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Test
    void shouldGetWorkingHoursWhenWorkloadExists() {
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter(USERNAME, 2025, JUNE);
        TrainerWorkload workload = buildTrainerWorkload(60);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(workload));

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isEqualTo(60);
    }

    @Test
    void shouldReturnZeroWorkingHoursWhenWorkloadDoesNotExist() {
        TrainerWorkloadSearchFilter filter = new TrainerWorkloadSearchFilter("nonexistent.trainer", 2025, JUNE);

        when(repository.findByUsername("nonexistent.trainer")).thenReturn(Optional.empty());

        Integer actual = service.getWorkingHours(filter);

        assertThat(actual).isZero();
    }

    @Test
    void shouldCreateNewWorkloadOnAddWhenNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(ADD);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        service.updateWorkload(update);

        verify(repository).update(any(TrainerWorkload.class));
    }

    @Test
    void shouldDoNothingOnDeleteWhenNotExist() {
        TrainerWorkloadUpdate update = buildUpdate(DELETE);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        service.updateWorkload(update);

        verify(repository, never()).update(any(TrainerWorkload.class));
    }

    @Test
    void shouldUpdateWorkloadOnAddWhenExists() {
        TrainerWorkloadUpdate update = new TrainerWorkloadUpdate(USERNAME, "NewFirstName", "NewLastName", false,
                TrainingDate.of(2025, JUNE), 60, ADD);
        TrainerWorkload workload = buildTrainerWorkload(60);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateWorkload(update);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).update(captor.capture());
        TrainerWorkload captured = captor.getValue();
        assertThat(captured.getFirstName()).isEqualTo("NewFirstName");
        assertThat(captured.getLastName()).isEqualTo("NewLastName");
        assertThat(captured.getIsActive()).isFalse();
    }

    @Test
    void shouldUpdateWorkloadOnDeleteWhenExists() {
        TrainerWorkloadUpdate update = new TrainerWorkloadUpdate(USERNAME, "NewFirstName", "NewLastName", false,
                TrainingDate.of(2025, JUNE), 40, DELETE);
        TrainerWorkload workload = buildTrainerWorkload(100);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(workload));

        service.updateWorkload(update);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).update(captor.capture());
        TrainerWorkload captured = captor.getValue();
        assertThat(captured.getFirstName()).isEqualTo("NewFirstName");
        assertThat(captured.getLastName()).isEqualTo("NewLastName");
        assertThat(captured.getIsActive()).isFalse();
    }

    private TrainerWorkloadUpdate buildUpdate(ActionType actionType) {
        return new TrainerWorkloadUpdate(USERNAME, "Marcus", "Stone", true,
                TrainingDate.of(2025, JUNE), 60, actionType);
    }

    private TrainerWorkload buildTrainerWorkload(int workingHours) {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(JUNE)
                .workingHours(workingHours)
                .build();
        YearSummary yearSummary = YearSummary.builder()
                .year(2025)
                .months(List.of(monthSummary))
                .build();

        return TrainerWorkload.builder()
                .username(USERNAME)
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .years(List.of(yearSummary))
                .build();
    }

}
