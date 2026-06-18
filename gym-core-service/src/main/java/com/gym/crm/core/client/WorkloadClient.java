package com.gym.crm.core.client;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/trainer-workloads")
public interface WorkloadClient {

    @PostExchange
    void updateTrainerWorkload(@RequestBody TrainerWorkloadUpdateRequest request);

}
