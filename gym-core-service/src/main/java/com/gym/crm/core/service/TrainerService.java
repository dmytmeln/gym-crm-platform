package com.gym.crm.core.service;

import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;

import java.util.List;

public interface TrainerService {

    Trainer createTrainer(Trainer trainer);

    Trainer getTrainerByUsername(String username);

    List<Training> getTrainerTrainings(TrainerTrainingSearchFilter filter);

    boolean doesUsernameAndPasswordMatch(String username, String password);

    Trainer updateTrainer(Trainer trainer);

    void updateActivationStatus(String username, boolean isActive);

}
