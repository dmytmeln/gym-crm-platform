package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginCounterTest {

    private MeterRegistry registry;
    private LoginCounter counter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        counter = new LoginCounter(registry);
    }

    @Test
    void shouldIncrementSuccess() {
        counter.increment(true);

        double actual = registry.counter("gym_crm_login_attempts_total", "status", "success").count();
        assertEquals(1.0, actual);
    }

    @Test
    void shouldIncrementFailure() {
        counter.increment(false);

        double actual = registry.counter("gym_crm_login_attempts_total", "status", "failure").count();
        assertEquals(1.0, actual);
    }

}
