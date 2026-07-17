package com.gym.crm.bdd.workload.steps;

import com.gym.crm.bdd.workload.client.TrainerWorkloadQueuePublisher;
import com.gym.crm.bdd.workload.client.WorkloadRestClient;
import com.gym.crm.bdd.workload.model.WorkloadAction;
import com.gym.crm.bdd.workload.model.WorkloadUpdatePayload;
import com.gym.crm.bdd.workload.support.JwtTokenFactory;
import com.gym.crm.bdd.workload.support.WorkloadComponentStack;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static com.gym.crm.bdd.workload.model.WorkloadAction.ADD;
import static com.gym.crm.bdd.workload.model.WorkloadAction.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkloadSteps {

    private static final int HTTP_OK = 200;
    private static final int UNIQUE_SUFFIX_LENGTH = 8;
    private static final Duration UPDATE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);
    private static final Duration NEGATIVE_OBSERVATION = Duration.ofSeconds(2);

    private final TrainerWorkloadQueuePublisher workloadQueuePublisher = new TrainerWorkloadQueuePublisher();
    private final WorkloadRestClient workloadRestClient = new WorkloadRestClient(WorkloadComponentStack.baseUrl());

    private String username;
    private String accessToken;
    private LocalDate trainingDate;
    private int trainingDuration;
    private WorkloadAction action;

    @Given("authenticated trainer")
    public void authenticatedTrainer() throws Exception {
        username = createUniqueTrainerUsername();
        accessToken = JwtTokenFactory.create(username);
    }

    @Given("ADD workload update for {int} minutes in {word} {int}")
    public void addWorkloadUpdate(int duration, String month, int year) {
        trainingDuration = duration;
        trainingDate = LocalDate.of(year, Month.valueOf(month), 15);
        action = ADD;
    }

    @Given("DELETE workload update for {int} minutes in {word} {int}")
    public void deleteWorkloadUpdate(int duration, String month, int year) {
        trainingDuration = duration;
        trainingDate = LocalDate.of(year, Month.valueOf(month), 15);
        action = DELETE;
    }

    @Given("existing workload of {int} minutes in {word} {int}")
    public void existingWorkload(int duration, String month, int year) throws Exception {
        trainingDate = LocalDate.of(year, Month.valueOf(month), 15);
        WorkloadUpdatePayload workloadUpdate = createWorkloadUpdate(duration, ADD);
        workloadQueuePublisher.publishUpdate(workloadUpdate);
        awaitMonthlyWorkload(duration);
    }

    @When("workload update is published")
    public void workloadUpdateIsPublished() throws Exception {
        WorkloadUpdatePayload workloadUpdate = createWorkloadUpdate(trainingDuration, action);
        workloadQueuePublisher.publishUpdate(workloadUpdate);
    }

    @Then("trainer monthly workload becomes {int} minutes")
    public void trainerMonthlyWorkloadBecomes(int expectedMinutes) {
        awaitMonthlyWorkload(expectedMinutes);
    }

    @Then("trainer monthly workload remains {int} minutes")
    public void trainerMonthlyWorkloadRemains(int expectedMinutes) {
        await()
                .alias("trainer monthly workload remains " + expectedMinutes + " minutes")
                .during(NEGATIVE_OBSERVATION)
                .atMost(UPDATE_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertMonthlyWorkload(expectedMinutes));
    }

    private void awaitMonthlyWorkload(int expectedMinutes) {
        await()
                .alias("trainer monthly workload becomes " + expectedMinutes + " minutes")
                .atMost(UPDATE_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertMonthlyWorkload(expectedMinutes));
    }

    private void assertMonthlyWorkload(int expectedMinutes) {
        Response response = getMonthlyWorkload();
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
        assertThat(response.jsonPath().getInt("trainingWorkingHours")).isEqualTo(expectedMinutes);
    }

    private Response getMonthlyWorkload() {
        return workloadRestClient.getMonthlyWorkload(username,
                trainingDate.getYear(),
                trainingDate.getMonth().name(),
                accessToken);
    }

    private String createUniqueTrainerUsername() {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, UNIQUE_SUFFIX_LENGTH);

        return "component.trainer" + suffix;
    }

    private WorkloadUpdatePayload createWorkloadUpdate(int duration, WorkloadAction workloadAction) {
        return new WorkloadUpdatePayload(username,
                "Component",
                "Trainer",
                true,
                trainingDate,
                duration,
                workloadAction);
    }

}
