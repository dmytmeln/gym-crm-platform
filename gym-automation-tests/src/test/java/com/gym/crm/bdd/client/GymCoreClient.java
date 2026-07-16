package com.gym.crm.bdd.client;

import com.gym.crm.bdd.model.CreateTraineeRequest;
import com.gym.crm.bdd.model.CreateTrainerRequest;
import com.gym.crm.bdd.support.GymCoreComponentStack;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.NoArgsConstructor;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

@NoArgsConstructor
public class GymCoreClient {

    private static final int HTTP_OK = 200;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TRAINERS_BASE = "/trainers";
    private static final String TRAINEES_BASE = "/trainees";
    private static final String AUTH_BASE = "/auth";
    private static final String TRAININGS_BASE = "/trainings";

    public Response registerTrainer(CreateTrainerRequest request) {
        return request()
                .contentType(JSON)
                .body(request)
                .post(TRAINERS_BASE + "/register");
    }

    public Response registerTrainee(CreateTraineeRequest request) {
        return request()
                .contentType(JSON)
                .body(request)
                .post(TRAINEES_BASE + "/register");
    }

    public String login(String username, String password) {
        return request()
                .contentType(JSON)
                .body(Map.of("username", username,
                        "password", password))
                .post(AUTH_BASE + "/login")
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .header(AUTHORIZATION_HEADER)
                .substring(BEARER_PREFIX.length());
    }

    public Response createTraining(Object body, String contentType, String token) {
        return request(token)
                .contentType(contentType)
                .body(body)
                .post(TRAININGS_BASE);
    }

    public Response trainerTrainings(String username, String token) {
        return request(token)
                .get(TRAINERS_BASE + "/{username}/trainings", username);
    }

    public void logout(String token) {
        request(token)
                .post(AUTH_BASE + "/logout")
                .then()
                .statusCode(HTTP_OK);
    }

    public void deactivateTrainer(String username, String token) {
        request(token)
                .contentType(JSON)
                .body(Map.of("isActive", false))
                .patch(TRAINERS_BASE + "/{username}/activation", username)
                .then()
                .statusCode(HTTP_OK);
    }

    private RequestSpecification request() {
        return given().baseUri(GymCoreComponentStack.baseUrl());
    }

    private RequestSpecification request(String token) {
        RequestSpecification spec = request();
        if (token != null) {
            spec.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
        }

        return spec;
    }

}
