package com.gym.crm.core.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class QueryTimerAspect {

    private static final String METRIC_NAME = "gym_crm_search_duration_seconds";
    private static final String TAG_KEY = "search_type";

    private final MeterRegistry registry;

    @Pointcut("execution(* com.gym.crm.core.service.TraineeService.getTraineeTrainings(..))")
    private void traineeQuery() {
    }

    @Pointcut("execution(* com.gym.crm.core.service.TrainerService.getTrainerTrainings(..))")
    private void trainerQuery() {
    }

    @Around("traineeQuery()")
    public Object profileTraineeQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(registry);

        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(registry.timer(METRIC_NAME, TAG_KEY, "trainee"));
        }
    }

    @Around("trainerQuery()")
    public Object profileTrainerQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(registry);

        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(registry.timer(METRIC_NAME, TAG_KEY, "trainer"));
        }
    }

}
