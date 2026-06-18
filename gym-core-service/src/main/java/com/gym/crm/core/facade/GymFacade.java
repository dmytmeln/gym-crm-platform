package com.gym.crm.core.facade;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.mapper.AuthMapper;
import com.gym.crm.core.mapper.TraineeMapper;
import com.gym.crm.core.mapper.TrainerMapper;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.service.AuthenticationService;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.common.BusinessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GymFacade {

    private static final String USERNAME_NULL_MSG = "Username cannot be null";

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;
    private final BusinessValidator validator;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final AuthMapper authMapper;

    public String login(LoginRequest loginRequest) {
        Objects.requireNonNull(loginRequest, "LoginRequestDto cannot be null");
        validator.validate(loginRequest);
        LoginRequestDto dto = authMapper.toDto(loginRequest);
        validator.validate(dto);

        return authenticationService.login(dto);
    }

    public void logout(String token) {
        Objects.requireNonNull(token, "Token cannot be null");

        authenticationService.logout(token);
    }

    public void changePassword(LoginChangeRequest loginChangeRequest) {
        Objects.requireNonNull(loginChangeRequest, "LoginChangeRequest cannot be null");
        validator.validate(loginChangeRequest);
        LoginChangeDto loginChangeDto = authMapper.toDto(loginChangeRequest);
        validator.validate(loginChangeDto);

        authenticationService.changePassword(loginChangeDto);
    }

    public TraineeCreateResponse createTrainee(TraineeCreateRequest request) {
        Objects.requireNonNull(request, "TraineeCreateRequest cannot be null");
        validator.validate(request);

        Trainee trainee = traineeMapper.toEntity(request);
        Trainee createdTrainee = traineeService.createTrainee(trainee);

        return traineeMapper.toCreateResponse(createdTrainee);
    }

    public TraineeGetResponse getTraineeByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        Trainee trainee = traineeService.getTraineeByUsername(username);
        return traineeMapper.toGetResponse(trainee);
    }

    public List<AssignedTrainerResponse> getAvailableTrainersForTrainee(String username) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        List<Trainer> trainers = traineeService.getAvailableTrainers(username);
        return traineeMapper.toAssignedTrainerResponseListFromList(trainers);
    }

    public List<GetTraineeTrainingResponse> getTraineeTrainings(TraineeTrainingSearchFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        validator.validate(filter);

        List<Training> trainings = traineeService.getTraineeTrainings(filter);
        return traineeMapper.toGetTraineeTrainingResponseList(trainings);
    }

    public TraineeUpdateResponse updateTrainee(String username, TraineeUpdateRequest request) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        Objects.requireNonNull(request, "TraineeUpdateRequest cannot be null");
        validator.validate(request);

        Trainee trainee = traineeMapper.toEntity(request, username);
        Trainee updatedTrainee = traineeService.updateTrainee(trainee);

        return traineeMapper.toUpdateResponse(updatedTrainee);
    }

    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainers(String username, TraineeAssignedTrainersUpdateRequest request) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        Objects.requireNonNull(request, "TraineeAssignedTrainersUpdateRequest cannot be null");

        Trainee updatedTrainee = traineeService.updateTraineeTrainers(username, request.getTrainerUsernames());
        return traineeMapper.toAssignedTrainersUpdateResponse(updatedTrainee.getTrainers());
    }

    public void updateTraineeActivationStatus(String username, ActivationStatusRequest request) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        Objects.requireNonNull(request, "ActivationStatusRequest cannot be null");

        traineeService.updateActivationStatus(username, request.getIsActive());
    }

    public boolean deleteTraineeByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        return traineeService.deleteTraineeByUsername(username);
    }

    public TrainerCreateResponse createTrainer(TrainerCreateRequest request) {
        Objects.requireNonNull(request, "TrainerCreateRequest cannot be null");
        validator.validate(request);

        Trainer trainer = trainerMapper.toEntity(request);
        Trainer createdTrainer = trainerService.createTrainer(trainer);

        return trainerMapper.toCreateResponse(createdTrainer);
    }

    public TrainerGetResponse getTrainerByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        Trainer trainer = trainerService.getTrainerByUsername(username);
        return trainerMapper.toGetResponse(trainer);
    }

    public TrainerUpdateResponse updateTrainer(String username, TrainerUpdateRequest request) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        Objects.requireNonNull(request, "TrainerUpdateRequest cannot be null");
        validator.validate(request);

        Trainer trainer = trainerMapper.toEntity(request, username);
        Trainer updatedTrainer = trainerService.updateTrainer(trainer);

        return trainerMapper.toUpdateResponse(updatedTrainer);
    }

    public void updateTrainerActivationStatus(String username, boolean isActive) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        trainerService.updateActivationStatus(username, isActive);
    }

    public List<GetTrainerTrainingResponse> getTrainerTrainings(TrainerTrainingSearchFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        validator.validate(filter);

        List<Training> trainings = trainerService.getTrainerTrainings(filter);
        return trainerMapper.toGetTrainerTrainingResponseList(trainings);
    }

    public void createTraining(TrainingCreateRequest request) {
        Objects.requireNonNull(request, "TrainingCreateRequest cannot be null");
        validator.validate(request);

        Training training = trainingMapper.toEntity(request);
        trainingService.createTraining(training);
    }

    public void deleteTraining(Long id, String trainerUsername) {
        Objects.requireNonNull(id, "Training ID cannot be null");
        Objects.requireNonNull(trainerUsername, "Trainer username cannot be null");

        trainingService.deleteTraining(id, trainerUsername);
    }

    public List<TrainingTypeResponse> getAllTrainingTypes() {
        List<TrainingType> trainingTypes = trainingService.getAllTrainingTypes();
        return trainingMapper.toTrainingTypeResponseList(trainingTypes);
    }

}
