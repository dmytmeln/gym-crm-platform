package com.gym.crm.core.dto.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class TrainerTrainingSearchFilter extends TrainingSearchFilter {
    private String traineeName;
}
