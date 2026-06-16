package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrainingCreatedCounter {

    private final MeterRegistry registry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public TrainingCreatedCounter(MeterRegistry registry) {
        this.registry = registry;
    }

    public void increment(String trainingType) {
        Counter counter = counters.computeIfAbsent(trainingType, this::createCounter);
        counter.increment();
    }

    private Counter createCounter(String trainingType) {
        return Counter.builder("gym_crm_trainings_created_total")
                .tag("training_type", trainingType)
                .description("Total training sessions created")
                .register(registry);
    }

}
