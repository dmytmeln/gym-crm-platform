package com.gym.crm.core.client;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.DownstreamConnectionException;
import com.gym.crm.core.exception.DownstreamServiceUnavailableException;
import com.gym.crm.core.exception.DownstreamTimeoutException;
import com.gym.crm.core.exception.ServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.ADD;
import static com.gia.openapi.model.TrainerWorkloadUpdateRequest.ActionTypeEnum.DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadClientFacade {

    private static final String SERVICE_NAME = "workload-service";

    private final WorkloadClient workloadClient;

    private int timeoutSeconds;

    @Value("${app.services.workload.timeout-seconds:3}")
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallbackUpdateWorkload")
    public void addWorkload(Training training) {
        updateWorkload(training, ADD);
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallbackUpdateWorkload")
    public void deleteWorkload(Training training) {
        updateWorkload(training, DELETE);
    }

    public void fallbackUpdateWorkload(Training training, Throwable t) {
        log.error("Failed to update trainer workload in workload-service for trainer {}: {}",
                training.getTrainer().getUser().getUsername(), t.getMessage(), t);
        throw mapToServiceException(t);
    }

    private void updateWorkload(Training training, ActionTypeEnum actionType) {
        User user = training.getTrainer().getUser();
        TrainerWorkloadUpdateRequest request = new TrainerWorkloadUpdateRequest()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(actionType);

        log.info("Sending workload update to workload-service: {} (action: {})", request.getUsername(), actionType);
        workloadClient.updateTrainerWorkload(request);
    }

    private ServiceException mapToServiceException(Throwable throwable) {
        Throwable cause = getRootCause(throwable);

        if (cause instanceof ServiceException serviceException) {
            return serviceException;
        }

        if (isTimeoutException(cause)) {
            return new DownstreamTimeoutException(SERVICE_NAME, timeoutSeconds, cause);
        }

        if (isConnectionException(cause) || cause instanceof ResourceAccessException) {
            return new DownstreamConnectionException(SERVICE_NAME, cause);
        }

        return new DownstreamServiceUnavailableException(SERVICE_NAME, cause);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        return current;
    }

    private boolean isTimeoutException(Throwable throwable) {
        return throwable instanceof HttpTimeoutException || throwable instanceof SocketTimeoutException;
    }

    private boolean isConnectionException(Throwable throwable) {
        return throwable instanceof ConnectException;
    }

}
