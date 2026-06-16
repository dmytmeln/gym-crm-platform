package com.gym.crm.core.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LoginCounter {

    private final Counter successCounter;
    private final Counter failureCounter;

    public LoginCounter(MeterRegistry registry) {
        this.successCounter = createCounter(registry, "success");
        this.failureCounter = createCounter(registry, "failure");
    }

    public void increment(boolean success) {
        if (success) {
            successCounter.increment();
        } else {
            failureCounter.increment();
        }
    }

    private Counter createCounter(MeterRegistry registry, String status) {
        return Counter.builder("gym_crm_login_attempts_total")
                .tag("status", status)
                .description("Total login attempts")
                .register(registry);
    }

}
