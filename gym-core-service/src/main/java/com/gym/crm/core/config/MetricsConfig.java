package com.gym.crm.core.config;

import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private static final String METRIC_NAME = "gym_crm_active_users";
    private static final String TAG_KEY = "role";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final MeterRegistry registry;

    @PostConstruct
    public void registerCustomMetrics() {
        Gauge.builder(METRIC_NAME, traineeRepository::countActiveTrainees)
                .tag(TAG_KEY, "trainee")
                .description("Real-time count of active trainee profiles")
                .register(registry);

        Gauge.builder(METRIC_NAME, trainerRepository::countActiveTrainers)
                .tag(TAG_KEY, "trainer")
                .description("Real-time count of active trainer profiles")
                .register(registry);
    }

}
