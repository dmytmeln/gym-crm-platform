package com.gym.crm.bdd.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.Set;

public final class TrainingRequest {

    private static final String TRAINEE_USERNAME = "traineeUsername";
    private static final String TRAINER_USERNAME = "trainerUsername";
    private static final String TRAINING_NAME = "trainingName";
    private static final String TRAINING_DATE = "trainingDate";
    private static final String TRAINING_DURATION = "trainingDuration";
    private static final Set<String> REQUIRED_FIELDS = Set.of(TRAINEE_USERNAME,
            TRAINER_USERNAME,
            TRAINING_NAME,
            TRAINING_DATE,
            TRAINING_DURATION);

    private final ObjectNode body;

    private TrainingRequest(ObjectNode body) {
        this.body = body;
    }

    public static TrainingRequest valid(String traineeUsername,
                                        String trainerUsername,
                                        String trainingName,
                                        LocalDate trainingDate,
                                        int trainingDuration) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put(TRAINEE_USERNAME, traineeUsername);
        body.put(TRAINER_USERNAME, trainerUsername);
        body.put(TRAINING_NAME, trainingName);
        body.put(TRAINING_DATE, trainingDate.toString());
        body.put(TRAINING_DURATION, trainingDuration);

        return new TrainingRequest(body);
    }

    public void useTrainer(String username) {
        body.put(TRAINER_USERNAME, username);
    }

    public void useTrainee(String username) {
        body.put(TRAINEE_USERNAME, username);
    }

    public void omitRequiredField(String fieldName) {
        if (!REQUIRED_FIELDS.contains(fieldName)) {
            throw new IllegalArgumentException("Unknown required Training field: " + fieldName);
        }

        body.remove(fieldName);
    }

    public void setName(String name) {
        body.put(TRAINING_NAME, name);
    }

    public void setDate(String date) {
        body.put(TRAINING_DATE, date);
    }

    public void setDuration(int duration) {
        body.put(TRAINING_DURATION, duration);
    }

    public String name() {
        return body.path(TRAINING_NAME).asText();
    }

    public String date() {
        return body.path(TRAINING_DATE).asText();
    }

    public int duration() {
        return body.path(TRAINING_DURATION).asInt();
    }

    public ObjectNode body() {
        return body;
    }

}
