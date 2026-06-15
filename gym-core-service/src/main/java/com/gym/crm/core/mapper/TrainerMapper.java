package com.gym.crm.core.mapper;

import com.gia.openapi.model.AssignedTraineeResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface TrainerMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    TrainerCreateResponse toCreateResponse(Trainer trainer);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", source = "trainees")
    TrainerGetResponse toGetResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", source = "trainees")
    TrainerUpdateResponse toUpdateResponse(Trainer trainer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.isActive", constant = "true")
    @Mapping(target = "specialization.trainingTypeName", source = "specialization")
    Trainer toEntity(TrainerCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "user", expression = "java(buildUserForUpdate(request, username))")
    @Mapping(target = "specialization", ignore = true)
    Trainer toEntity(TrainerUpdateRequest request, String username);

    default User buildUserForUpdate(TrainerUpdateRequest request, String username) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .isActive(request.getIsActive())
                .build();
    }

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    AssignedTraineeResponse toAssignedTraineeResponse(Trainee trainee);

    List<AssignedTraineeResponse> toAssignedTraineeResponseList(Set<Trainee> trainees);

    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "traineeName", source = "trainee.user.username")
    GetTrainerTrainingResponse toGetTrainerTrainingResponse(Training training);

    List<GetTrainerTrainingResponse> toGetTrainerTrainingResponseList(List<Training> trainings);

}
