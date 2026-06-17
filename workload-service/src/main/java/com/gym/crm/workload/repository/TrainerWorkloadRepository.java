package com.gym.crm.workload.repository;

import com.gym.crm.workload.model.TrainerWorkload;

import java.util.Optional;

public interface TrainerWorkloadRepository {

    Optional<TrainerWorkload> findByUsername(String username);

    void update(TrainerWorkload workload);

}
