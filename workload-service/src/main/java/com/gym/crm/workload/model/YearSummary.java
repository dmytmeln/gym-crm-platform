package com.gym.crm.workload.model;

import com.gym.crm.workload.dto.TrainingDate;
import lombok.Builder;
import lombok.Getter;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class YearSummary {

    private final Integer year;
    private final List<MonthSummary> months;

    private YearSummary(Integer year, List<MonthSummary> months) {
        this.year = year;
        this.months = months;
    }

    public static YearSummary of(TrainingDate date, int duration) {
        MonthSummary monthSummary = MonthSummary.of(date.getMonth(), duration);
        return new YearSummary(date.getYear(), List.of(monthSummary));
    }

    public YearSummary increaseWorkingHours(Month month, int delta) {
        List<MonthSummary> updated = this.hasMonth(month)
                ? this.increaseMonthWorkingHours(month, delta)
                : this.addMonthToExistingMonths(month, delta);

        return new YearSummary(this.year, updated);
    }

    public YearSummary decreaseWorkingHours(Month month, int delta) {
        if (!this.hasMonth(month)) {
            return this;
        }

        List<MonthSummary> updated = this.months.stream()
                .map(monthSummary -> monthSummary.getMonth() == month
                        ? monthSummary.decreaseWorkingHours(delta)
                        : monthSummary)
                .filter(monthSummary -> monthSummary.getWorkingHours() > 0)
                .toList();

        return new YearSummary(this.year, updated);
    }

    private List<MonthSummary> addMonthToExistingMonths(Month month, int delta) {
        List<MonthSummary> existingMonths = new ArrayList<>(this.months);
        existingMonths.add(MonthSummary.of(month, delta));

        return existingMonths;
    }

    private List<MonthSummary> increaseMonthWorkingHours(Month month, int delta) {
        return this.months.stream()
                .map(monthSummary -> monthSummary.getMonth() == month
                        ? monthSummary.increaseWorkingHours(delta)
                        : monthSummary)
                .toList();
    }

    private boolean hasMonth(Month month) {
        return this.months.stream()
                .anyMatch(monthSummary -> monthSummary.getMonth() == month);
    }

}
