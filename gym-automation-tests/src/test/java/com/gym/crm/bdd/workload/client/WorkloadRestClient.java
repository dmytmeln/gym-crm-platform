package com.gym.crm.bdd.workload.client;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;

public class WorkloadRestClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final String baseUrl;

    public WorkloadRestClient(String baseUrl) {
        this.baseUrl = requireNonNull(baseUrl);
    }

    public Response getMonthlyWorkload(String username, int year, String month, String accessToken) {
        return given()
                .baseUri(baseUrl)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .queryParam("year", year)
                .queryParam("month", month)
                .get("/trainer-workloads/{username}", username);
    }

}
