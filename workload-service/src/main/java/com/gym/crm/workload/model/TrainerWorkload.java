package com.gym.crm.workload.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class TrainerWorkload {

    private final String username;
    private final String firstName;
    private final String lastName;
    private final Boolean isActive;
    private final List<YearSummary> years;

}
