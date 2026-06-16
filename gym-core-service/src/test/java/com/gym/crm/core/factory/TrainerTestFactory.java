package com.gym.crm.core.factory;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;

import java.util.HashSet;
import java.util.Set;

public class TrainerTestFactory {

    public static final Long DEFAULT_TRAINER_ID = 1L;
    public static final Long DEFAULT_USER_ID = 1L;
    public static final String DEFAULT_USERNAME = "marcus.stone";
    public static final String DEFAULT_FIRST_NAME = "Marcus";
    public static final String DEFAULT_LAST_NAME = "Stone";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final boolean DEFAULT_ACTIVE = true;
    public static final Long DEFAULT_SPECIALIZATION_ID = 1L;
    public static final String DEFAULT_SPECIALIZATION = "CARDIO";

    public static Trainer.TrainerBuilder getDefaultTrainerBuilder() {
        return Trainer.builder()
                .id(DEFAULT_TRAINER_ID)
                .user(buildUser())
                .specialization(buildTrainingType());
    }

    public static TrainingType.TrainingTypeBuilder getDefaultTrainingTypeBuilder() {
        return TrainingType.builder()
                .id(DEFAULT_SPECIALIZATION_ID)
                .trainingTypeName(DEFAULT_SPECIALIZATION);
    }

    public static User.UserBuilder getDefaultUserBuilder() {
        return User.builder()
                .username(DEFAULT_USERNAME)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .password(DEFAULT_PASSWORD)
                .isActive(DEFAULT_ACTIVE);
    }

    public static User buildUser() {
        return getDefaultUserBuilder().build();
    }

    public static TrainingType buildTrainingType() {
        return getDefaultTrainingTypeBuilder().build();
    }

    public static User buildUser(String username, String firstName, String lastName) {
        return getDefaultUserBuilder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static User buildUser(String username) {
        return getDefaultUserBuilder()
                .username(username)
                .build();
    }

    public static User buildUserWithoutCredentials() {
        return getDefaultUserBuilder()
                .username(null)
                .password(null)
                .build();
    }

    public static Trainer buildTrainerWithId(Long trainerId) {
        return getDefaultTrainerBuilder()
                .id(trainerId)
                .build();
    }

    public static Trainer buildTrainerWithId() {
        return getDefaultTrainerBuilder()
                .id(DEFAULT_TRAINER_ID)
                .build();
    }

    public static Trainer buildTrainerWithIdAndUserId() {
        return getDefaultTrainerBuilder()
                .id(DEFAULT_TRAINER_ID)
                .user(getDefaultUserBuilder().id(DEFAULT_USER_ID).build())
                .build();
    }

    public static Trainer buildTrainerWithoutCredentials() {
        return getDefaultTrainerBuilder()
                .user(buildUserWithoutCredentials())
                .build();
    }

    public static Trainer buildTrainerWithTrainees() {
        Trainee trainee = buildTraineeForTrainer();
        Set<Trainee> trainees = new HashSet<>();
        trainees.add(trainee);

        return getDefaultTrainerBuilder()
                .trainees(trainees)
                .build();
    }

    public static Trainer buildTrainerWithInactiveStatusAndTrainees() {
        Trainee trainee = buildTraineeForTrainer();
        Set<Trainee> trainees = new HashSet<>();
        trainees.add(trainee);

        return getDefaultTrainerBuilder()
                .user(getDefaultUserBuilder().isActive(false).build())
                .trainees(trainees)
                .build();
    }

    private static Trainee buildTraineeForTrainer() {
        User user = User.builder()
                .username(TraineeTestFactory.DEFAULT_USERNAME)
                .firstName(TraineeTestFactory.DEFAULT_FIRST_NAME)
                .lastName(TraineeTestFactory.DEFAULT_LAST_NAME)
                .build();

        return Trainee.builder()
                .user(user)
                .build();
    }

}
