package com.gym.crm.bdd.workload.client;

import com.gym.crm.bdd.workload.support.WorkloadComponentStack;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class WorkloadRestClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public Response getMonthlyWorkload(String username, int year, String month, String accessToken) {
        return given()
                .baseUri(WorkloadComponentStack.baseUrl())
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .queryParam("year", year)
                .queryParam("month", month)
                .get("/trainer-workloads/{username}", username);
    }

}
