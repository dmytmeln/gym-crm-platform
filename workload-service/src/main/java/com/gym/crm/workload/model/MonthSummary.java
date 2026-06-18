package com.gym.crm.workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Month;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = PRIVATE)
public class MonthSummary {

    private final Month month;
    private final Integer workingHours;

    public static MonthSummary of(Month month, int duration) {
        return new MonthSummary(month, duration);
    }

    public MonthSummary increaseWorkingHours(int delta) {
        return new MonthSummary(this.month, this.workingHours + delta);
    }

    public MonthSummary decreaseWorkingHours(int delta) {
        return new MonthSummary(this.month, Math.max(0, this.workingHours - delta));
    }

}
