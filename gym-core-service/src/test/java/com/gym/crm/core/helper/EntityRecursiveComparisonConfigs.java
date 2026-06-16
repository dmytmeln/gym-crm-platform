package com.gym.crm.core.helper;

import com.gym.crm.core.entity.Trainee_;
import com.gym.crm.core.entity.Trainer_;
import com.gym.crm.core.entity.TrainingType_;
import com.gym.crm.core.entity.Training_;
import com.gym.crm.core.entity.User_;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import static java.lang.String.format;

public class EntityRecursiveComparisonConfigs {

    public static RecursiveComparisonConfiguration getTrainingTypeConfigForDirectFields() {
        return getSharedBuilder()
                .withComparedFields(TrainingType_.ID, TrainingType_.TRAINING_TYPE_NAME)
                .build();
    }

    public static RecursiveComparisonConfiguration getTrainerConfigForDirectFields() {
        return getSharedBuilder()
                .withComparedFields(Trainer_.ID)
                .build();
    }

    public static RecursiveComparisonConfiguration getTrainerConfigForSaved() {
        return getSharedBuilder()
                .withComparedFields(Trainer_.USER)
                .withIgnoredFields(Trainer_.ID, format("%s.%s", Trainer_.USER, User_.ID))
                .build();
    }

    public static RecursiveComparisonConfiguration getTrainerConfigForExisting() {
        return getSharedBuilder()
                .withComparedFields(Trainer_.ID, Trainer_.USER)
                .build();
    }

    public static RecursiveComparisonConfiguration getTraineeConfigForDirectFields() {
        return getSharedBuilder()
                .withComparedFields(Trainee_.ID, Trainee_.DATE_OF_BIRTH, Trainee_.ADDRESS)
                .build();
    }

    public static RecursiveComparisonConfiguration getTraineeConfigForSaved() {
        return getSharedBuilder()
                .withComparedFields(Trainee_.DATE_OF_BIRTH, Trainee_.ADDRESS, Trainee_.USER)
                .withIgnoredFields(Trainee_.ID, format("%s.%s", Trainee_.USER, User_.ID))
                .build();
    }

    public static RecursiveComparisonConfiguration getTraineeConfigForExisting() {
        return getSharedBuilder()
                .withComparedFields(Trainee_.ID, Trainee_.DATE_OF_BIRTH, Trainee_.ADDRESS, Trainee_.USER)
                .build();
    }

    public static RecursiveComparisonConfiguration getTrainingConfigForSaved() {
        return getSharedBuilder()
                .withComparedFields(Training_.TRAINING_NAME, Training_.TRAINING_DATE, Training_.TRAINING_DURATION)
                .withIgnoredFields(Training_.ID)
                .build();
    }

    public static RecursiveComparisonConfiguration getTrainingConfigForExisting() {
        return getSharedBuilder()
                .withComparedFields(Training_.ID, Training_.TRAINING_NAME, Training_.TRAINING_DATE, Training_.TRAINING_DURATION)
                .build();
    }

    private static RecursiveComparisonConfiguration.Builder getSharedBuilder() {
        return RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("createdAt", "updatedAt", "version")
                .withIgnoreCollectionOrder(true);
    }

}
