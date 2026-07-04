package com.gym.crm.workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Month;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.data.mongodb.core.mapping.Field.Write.NON_NULL;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = PRIVATE)
public class MonthSummary {

    @Field(name = "month", write = NON_NULL)
    @NotNull
    private final Month month;

    @Field(name = "working_hours", write = NON_NULL)
    @NotNull
    @Min(0)
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
