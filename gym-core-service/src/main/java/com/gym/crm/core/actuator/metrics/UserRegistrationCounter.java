package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationCounter {

    private final Counter traineeSuccess;
    private final Counter traineeFailure;
    private final Counter trainerSuccess;
    private final Counter trainerFailure;

    public UserRegistrationCounter(MeterRegistry registry) {
        this.traineeSuccess = createCounter(registry, "trainee", "success");
        this.traineeFailure = createCounter(registry, "trainee", "failure");
        this.trainerSuccess = createCounter(registry, "trainer", "success");
        this.trainerFailure = createCounter(registry, "trainer", "failure");
    }

    public void incrementTrainee(boolean success) {
        if (success) {
            traineeSuccess.increment();
        } else {
            traineeFailure.increment();
        }
    }

    public void incrementTrainer(boolean success) {
        if (success) {
            trainerSuccess.increment();
        } else {
            trainerFailure.increment();
        }
    }

    private Counter createCounter(MeterRegistry registry, String role, String status) {
        return Counter.builder("gym_crm_user_registrations_total")
                .tag("role", role)
                .tag("status", status)
                .description("Total user profile registrations")
                .register(registry);
    }

}
