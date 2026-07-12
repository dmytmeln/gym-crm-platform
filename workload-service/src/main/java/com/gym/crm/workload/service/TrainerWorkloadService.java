package com.gym.crm.workload.service;

import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import jakarta.validation.Valid;

public interface TrainerWorkloadService {

    void updateWorkload(@Valid TrainerWorkloadUpdate update);

    Integer getWorkingHours(TrainerWorkloadSearchFilter filter);

}
