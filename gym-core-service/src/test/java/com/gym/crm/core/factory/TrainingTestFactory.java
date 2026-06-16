package com.gym.crm.core.factory;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;

import java.time.LocalDate;

import static java.time.Month.APRIL;

public class TrainingTestFactory {

    public static final Long DEFAULT_TRAINING_ID = 1L;
    public static final Long DEFAULT_TRAINEE_ID = 1L;
    public static final Long DEFAULT_TRAINER_ID = 2L;
    public static final String DEFAULT_TRAINING_NAME = "Morning Workout";
    public static final int DEFAULT_DURATION = 60;
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2026, APRIL, 15);
    public static final Long DEFAULT_TRAINING_TYPE_ID = 1L;
    public static final String DEFAULT_TRAINING_TYPE_NAME = "STRENGTH";

    public static Training.TrainingBuilder getDefaultTrainingBuilder() {
        return Training.builder()
                .trainee(Trainee.builder().id(DEFAULT_TRAINEE_ID).build())
                .trainer(Trainer.builder().id(DEFAULT_TRAINER_ID).build())
                .trainingName(DEFAULT_TRAINING_NAME)
                .trainingType(TrainingType.builder().
                        id(DEFAULT_TRAINING_TYPE_ID)
                        .trainingTypeName(DEFAULT_TRAINING_TYPE_NAME).
                        build())
                .trainingDuration(DEFAULT_DURATION)
                .trainingDate(DEFAULT_DATE);
    }

    public static Training buildTraining() {
        return getDefaultTrainingBuilder().build();
    }

    public static Training buildTraining(Long id, Long traineeId, Long trainerId) {
        return getDefaultTrainingBuilder()
                .id(id)
                .trainee(Trainee.builder().id(traineeId).build())
                .trainer(Trainer.builder().id(trainerId).build())
                .build();
    }

    public static Training buildTraining(Long id, String trainingName) {
        return getDefaultTrainingBuilder()
                .id(id)
                .trainingName(trainingName)
                .build();
    }

    public static Training buildTrainingWithId(Long trainingId) {
        return getDefaultTrainingBuilder().id(trainingId).build();
    }

}
