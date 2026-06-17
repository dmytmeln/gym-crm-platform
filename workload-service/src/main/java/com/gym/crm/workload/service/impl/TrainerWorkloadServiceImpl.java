package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.workload.dto.ActionType.DELETE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;

    @Override
    public Integer getWorkingHours(TrainerWorkloadSearchFilter filter) {
        log.info("Getting working hours for trainer: {}, year: {}, month: {}", filter.username(), filter.year(), filter.month());
        return repository.findByUsername(filter.username())
                .flatMap(workload -> findYearSummary(workload, filter.year()))
                .flatMap(year -> findMonthSummary(year, filter.month()))
                .map(MonthSummary::getWorkingHours)
                .orElse(0);
    }

    @Override
    public void updateWorkload(TrainerWorkloadUpdate update) {
        log.info("Updating workload for trainer: {}, action: {}, duration: {} min", update.username(), update.actionType(), update.trainingDuration());
        Optional<TrainerWorkload> workloadOpt = repository.findByUsername(update.username());

        boolean isDeleteForNonExistentWorkload = update.actionType() == DELETE && workloadOpt.isEmpty();
        if (isDeleteForNonExistentWorkload) {
            log.info("Ignore delete action for non-existent workload of trainer: {}", update.username());
            return;
        }

        TrainerWorkload workload = workloadOpt.map(existing -> refreshTrainerWorkload(existing, update))
                .orElseGet(() -> createWorkload(update));

        TrainerWorkload updatedWorkload = switch (update.actionType()) {
            case ADD -> workload.increaseWorkingHours(update.trainingDate(), update.trainingDuration());
            case DELETE -> workload.decreaseWorkingHours(update.trainingDate(), update.trainingDuration());
        };

        repository.update(updatedWorkload);
        log.info("Successfully updated workload for trainer: {}", update.username());
    }

    private Optional<YearSummary> findYearSummary(TrainerWorkload workload, Integer year) {
        return workload.getYears().stream()
                .filter(yearSummary -> yearSummary.getYear().equals(year))
                .findFirst();
    }

    private Optional<MonthSummary> findMonthSummary(YearSummary year, Month month) {
        return year.getMonths().stream()
                .filter(monthSummary -> monthSummary.getMonth() == month)
                .findFirst();
    }

    private TrainerWorkload refreshTrainerWorkload(TrainerWorkload existing, TrainerWorkloadUpdate update) {
        return existing.toBuilder()
                .firstName(update.firstName())
                .lastName(update.lastName())
                .isActive(update.isActive())
                .build();
    }

    private TrainerWorkload createWorkload(TrainerWorkloadUpdate update) {
        return TrainerWorkload.builder()
                .username(update.username())
                .firstName(update.firstName())
                .lastName(update.lastName())
                .isActive(update.isActive())
                .years(List.of())
                .build();
    }

}
