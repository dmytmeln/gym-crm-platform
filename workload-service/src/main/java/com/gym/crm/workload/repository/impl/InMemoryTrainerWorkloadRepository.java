package com.gym.crm.workload.repository.impl;

import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTrainerWorkloadRepository implements TrainerWorkloadRepository {

    private final Map<String, TrainerWorkload> database = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(database.get(username));
    }

    @Override
    public void update(TrainerWorkload workload) {
        database.put(workload.getUsername(), workload);
    }

}
