package com.gym.crm.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TrainingDate {

    private final int year;
    private final Month month;

    public static TrainingDate from(LocalDate date) {
        return new TrainingDate(date.getYear(), date.getMonth());
    }

    public static TrainingDate of(int year, Month month) {
        return new TrainingDate(year, month);
    }

}
