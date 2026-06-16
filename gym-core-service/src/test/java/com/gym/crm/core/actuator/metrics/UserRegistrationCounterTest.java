package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRegistrationCounterTest {

    private MeterRegistry registry;
    private UserRegistrationCounter counter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        counter = new UserRegistrationCounter(registry);
    }

    @Test
    void shouldIncrementTraineeSuccess() {
        counter.incrementTrainee(true);

        double actual = registry.counter("gym_crm_user_registrations_total", "role", "trainee", "status", "success").count();
        assertEquals(1.0, actual);
    }

    @Test
    void shouldIncrementTraineeFailure() {
        counter.incrementTrainee(false);

        double actual = registry.counter("gym_crm_user_registrations_total", "role", "trainee", "status", "failure").count();
        assertEquals(1.0, actual);
    }

    @Test
    void shouldIncrementTrainerSuccess() {
        counter.incrementTrainer(true);

        double actual = registry.counter("gym_crm_user_registrations_total", "role", "trainer", "status", "success").count();
        assertEquals(1.0, actual);
    }

    @Test
    void shouldIncrementTrainerFailure() {
        counter.incrementTrainer(false);

        double actual = registry.counter("gym_crm_user_registrations_total", "role", "trainer", "status", "failure").count();
        assertEquals(1.0, actual);
    }

}
