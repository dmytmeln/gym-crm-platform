package com.gym.crm.core.mapper;

import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
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
public interface TraineeMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.password")
    TraineeCreateResponse toCreateResponse(Trainee trainee);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    TraineeGetResponse toGetResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    TraineeUpdateResponse toUpdateResponse(Trainee trainee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.isActive", constant = "true")
    Trainee toEntity(TraineeCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "user", expression = "java(buildUser(request, username))")
    Trainee toEntity(TraineeUpdateRequest request, String username);

    default User buildUser(TraineeUpdateRequest request, String username) {
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
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    AssignedTrainerResponse toAssignedTrainerResponse(Trainer trainer);

    List<AssignedTrainerResponse> toAssignedTrainerResponseList(Set<Trainer> trainers);

    List<AssignedTrainerResponse> toAssignedTrainerResponseListFromList(List<Trainer> trainers);

    default TraineeAssignedTrainersUpdateResponse toAssignedTrainersUpdateResponse(Set<Trainer> trainers) {
        return new TraineeAssignedTrainersUpdateResponse().trainers(toAssignedTrainerResponseList(trainers));
    }

    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainerName", source = "trainer.user.username")
    GetTraineeTrainingResponse toGetTraineeTrainingResponse(Training training);

    List<GetTraineeTrainingResponse> toGetTraineeTrainingResponseList(List<Training> trainings);

}
