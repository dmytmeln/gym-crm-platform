package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingCreatedCounterTest {

    private MeterRegistry registry;
    private TrainingCreatedCounter counter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        counter = new TrainingCreatedCounter(registry);
    }

    @Test
    void shouldIncrementTrainingCreatedType() {
        counter.increment("cardio");

        double actual = registry.counter("gym_crm_trainings_created_total", "training_type", "cardio").count();
        assertEquals(1.0, actual);
    }

    @Test
    void shouldIncrementMultipleTimesForSameTrainingType() {
        counter.increment("yoga");
        counter.increment("yoga");

        double actual = registry.counter("gym_crm_trainings_created_total", "training_type", "yoga").count();
        assertEquals(2.0, actual);
    }

    @Test
    void shouldCountIndependentTrainingTypes() {
        counter.increment("cardio");
        counter.increment("yoga");
        counter.increment("yoga");

        double actualCardio = registry.counter("gym_crm_trainings_created_total", "training_type", "cardio").count();
        double actualYoga = registry.counter("gym_crm_trainings_created_total", "training_type", "yoga").count();
        assertEquals(1.0, actualCardio);
        assertEquals(2.0, actualYoga);
    }

}
