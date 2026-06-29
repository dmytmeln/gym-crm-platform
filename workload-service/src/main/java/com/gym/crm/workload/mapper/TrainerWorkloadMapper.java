package com.gym.crm.workload.mapper;

import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gym.crm.workload.contract.TrainerWorkloadUpdateMessage;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainingDate;
import org.mapstruct.Mapper;

import java.time.LocalDate;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TrainerWorkloadMapper {

    TrainerWorkloadUpdate toDomainUpdate(TrainerWorkloadUpdateRequest request);

    TrainerWorkloadUpdate toDomainUpdate(TrainerWorkloadUpdateMessage message);

    default TrainingDate toTrainingDate(LocalDate date) {
        return date != null ? TrainingDate.from(date) : null;
    }

}
