package com.gym.crm.workload.model;

import com.gym.crm.workload.dto.TrainingDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.data.mongodb.core.mapping.Field.Write.NON_NULL;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = PRIVATE)
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "trainer_name_idx", def = "{'first_name': 1, 'last_name': 1}")
public class TrainerWorkload {

    @Id
    @Field(name = "_id", write = NON_NULL)
    private final String username;

    @Field(name = "first_name", write = NON_NULL)
    private final String firstName;

    @Field(name = "last_name", write = NON_NULL)
    private final String lastName;

    @Field(name = "is_active", write = NON_NULL)
    private final Boolean isActive;

    @Field(name = "years", write = NON_NULL)
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
