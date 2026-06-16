package com.gym.crm.workload.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class YearSummary {

    private final Integer year;
    private final List<MonthSummary> months;

}
