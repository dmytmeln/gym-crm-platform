package com.gym.crm.workload.model;

import com.gym.crm.workload.dto.TrainingDate;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class TrainerWorkload {

    private final String username;
    private final String firstName;
    private final String lastName;
    private final Boolean isActive;
    private final List<YearSummary> years;

    public TrainerWorkload increaseWorkingHours(TrainingDate trainingDate, int duration) {
        List<YearSummary> updated = this.hasYear(trainingDate.getYear())
                ? this.increaseYearWorkingHours(trainingDate, duration)
                : this.addNewYearToExistingYears(trainingDate, duration);

        return this.toBuilder()
                .years(updated)
                .build();
    }

    public TrainerWorkload decreaseWorkingHours(TrainingDate trainingDate, int duration) {
        int yearKey = trainingDate.getYear();
        if (!this.hasYear(yearKey)) {
            return this;
        }

        List<YearSummary> updated = this.years.stream()
                .map(yearSummary -> yearSummary.getYear() == yearKey
                        ? yearSummary.decreaseWorkingHours(trainingDate.getMonth(), duration)
                        : yearSummary)
                .filter(yearSummary -> !yearSummary.getMonths().isEmpty())
                .toList();

        return this.toBuilder()
                .years(updated)
                .build();
    }

    private List<YearSummary> increaseYearWorkingHours(TrainingDate trainingDate, int duration) {
        return this.years.stream()
                .map(yearSummary -> yearSummary.getYear() == trainingDate.getYear()
                        ? yearSummary.increaseWorkingHours(trainingDate.getMonth(), duration)
                        : yearSummary)
                .toList();
    }

    private List<YearSummary> addNewYearToExistingYears(TrainingDate trainingDate, int duration) {
        List<YearSummary> existingYears = new ArrayList<>(this.years);
        existingYears.add(YearSummary.of(trainingDate, duration));

        return existingYears;
    }

    private boolean hasYear(int year) {
        return this.years.stream()
                .anyMatch(yearSummary -> yearSummary.getYear() == year);
    }

}
