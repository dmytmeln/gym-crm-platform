package com.gym.crm.core.mapper;

import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    @Mapping(target = "trainee.user.username", source = "traineeUsername")
    @Mapping(target = "trainer.user.username", source = "trainerUsername")
    Training toEntity(TrainingCreateRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "trainingTypeName")
    TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toTrainingTypeResponseList(List<TrainingType> trainingTypes);

}
