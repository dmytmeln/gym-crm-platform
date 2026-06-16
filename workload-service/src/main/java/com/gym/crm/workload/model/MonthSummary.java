package com.gym.crm.workload.model;

import java.time.Month;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MonthSummary {

    private final Month month;
    private final Integer summaryDuration;

}
