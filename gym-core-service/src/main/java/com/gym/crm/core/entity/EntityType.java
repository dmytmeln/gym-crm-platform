package com.gym.crm.core.entity;

import lombok.Getter;

@Getter
public enum EntityType {

    TRAINEE("Trainee"),
    TRAINER("Trainer"),
    USER("User"),
    TRAINING("Training"),
    TRAINING_TYPE("TrainingType");


    private final String name;

    EntityType(String name) {
        this.name = name;
    }

}
