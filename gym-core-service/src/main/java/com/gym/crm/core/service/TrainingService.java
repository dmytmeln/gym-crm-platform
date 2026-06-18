package com.gym.crm.core.service;

import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;

import java.util.List;

public interface TrainingService {

    Training createTraining(Training training);

    List<TrainingType> getAllTrainingTypes();

    void deleteTraining(Long id, String trainerUsername);

}
