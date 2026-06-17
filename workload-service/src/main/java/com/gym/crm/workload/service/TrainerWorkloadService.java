package com.gym.crm.workload.service;

import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;

public interface TrainerWorkloadService {

    void updateWorkload(TrainerWorkloadUpdate update);

    Integer getWorkingHours(TrainerWorkloadSearchFilter filter);

}
