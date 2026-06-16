package com.gym.crm.core.service;

import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;

import java.util.List;

public interface TraineeService {

    Trainee createTrainee(Trainee trainee);

    Trainee getTraineeByUsername(String username);

    List<Trainer> getAvailableTrainers(String username);

    List<Training> getTraineeTrainings(TraineeTrainingSearchFilter filter);

    boolean doesUsernameAndPasswordMatch(String username, String password);

    Trainee updateTrainee(Trainee trainee);

    Trainee updateTraineeTrainers(String username, List<String> trainerUsernames);

    void updateActivationStatus(String username, boolean isActive);

    boolean deleteTraineeByUsername(String username);

}
