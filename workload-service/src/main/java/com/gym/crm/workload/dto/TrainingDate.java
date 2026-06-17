package com.gym.crm.workload.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;

@Getter
@Builder
public class TrainingDate {

    private final int year;
    private final Month month;

    private TrainingDate(int year, Month month) {
        this.year = year;
        this.month = month;
    }

    public static TrainingDate from(LocalDate date) {
        return new TrainingDate(date.getYear(), date.getMonth());
    }

    public static TrainingDate of(int year, Month month) {
        return new TrainingDate(year, month);
    }

}
