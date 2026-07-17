package com.gym.crm.bdd.integration.coreworkload.steps;

import com.gym.crm.bdd.gymcore.client.GymCoreClient;
import com.gym.crm.bdd.gymcore.model.CreateTraineeRequest;
import com.gym.crm.bdd.gymcore.model.CreateTrainerRequest;
import com.gym.crm.bdd.gymcore.model.TrainingRequest;
import com.gym.crm.bdd.integration.coreworkload.support.CoreWorkloadIntegrationStack;
import com.gym.crm.bdd.workload.client.WorkloadRestClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CoreWorkloadSteps {

    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int TRAINING_DURATION_MINUTES = 60;
    private static final int UNIQUE_SUFFIX_LENGTH = 8;
    private static final String APPLICATION_JSON = "application/json";
    private static final String UNKNOWN_TRAINEE_PREFIX = "unknown.";
    private static final Duration UPDATE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration NEGATIVE_OBSERVATION = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private final GymCoreClient gymCoreClient = new GymCoreClient(CoreWorkloadIntegrationStack.gymCoreBaseUrl());
    private final WorkloadRestClient workloadClient = new WorkloadRestClient(CoreWorkloadIntegrationStack.workloadBaseUrl());

    private RegisteredUser trainer;
    private RegisteredUser trainee;
    private LocalDate trainingDate;
    private String trainerToken;
    private Response response;

    @Given("unique trainer and trainee registered through Gym Core")
    public void uniqueParticipantsAreRegistered() {
        String suffix = uniqueSuffix();
        CreateTrainerRequest trainerRequest = new CreateTrainerRequest("Integration" + suffix, "Trainer", "CARDIO");
        trainer = registerTrainer(trainerRequest);

        CreateTraineeRequest traineeRequest = new CreateTraineeRequest("Integration" + suffix,
                "Trainee",
                null,
                null,
                "1995-05-10",
                "Integration Street");
        trainee = registerTrainee(traineeRequest);

        trainerToken = gymCoreClient.login(trainer.username(), trainer.password());

        trainingDate = LocalDate.now().plusMonths(1).withDayOfMonth(15);
    }

    @Given("{int}-minute Training exists and is reflected in Trainer Workload")
    public void trainingExistsAndIsReflected(int durationMinutes) {
        createTraining(trainee.username(), durationMinutes)
                .then()
                .statusCode(HTTP_OK);
        awaitMonthlyWorkload(durationMinutes);
    }

    @When("trainer creates {int}-minute Training through Gym Core")
    public void trainerCreatesTraining(int durationMinutes) {
        response = createTraining(trainee.username(), durationMinutes);
    }

    @When("trainee deletes profile through Gym Core")
    public void traineeDeletesTheirProfile() {
        String traineeToken = gymCoreClient.login(trainee.username(), trainee.password());
        response = gymCoreClient.deleteTrainee(trainee.username(), traineeToken);
    }

    @When("trainer creates Training for unknown trainee")
    public void trainerCreatesTrainingForUnknownTrainee() {
        String unknownTraineeUsername = UNKNOWN_TRAINEE_PREFIX + uniqueSuffix();
        response = createTraining(unknownTraineeUsername, TRAINING_DURATION_MINUTES);
    }

    @Then("Gym Core accepts operation")
    public void gymCoreAcceptsOperation() {
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
    }

    @Then("Gym Core rejects Training as not found")
    public void gymCoreRejectsTrainingWithNotFound() {
        assertThat(response.statusCode()).isEqualTo(HTTP_NOT_FOUND);
    }

    @Then("Trainer Workload eventually becomes {int} minutes")
    public void trainerWorkloadEventuallyBecomes(int expectedMinutes) {
        awaitMonthlyWorkload(expectedMinutes);
    }

    @Then("Trainer Workload remains {int} minutes during delivery window")
    public void trainerWorkloadRemainsForDeliveryWindow(int expectedMinutes) {
        await()
                .alias("trainer monthly workload remains " + expectedMinutes + " minutes")
                .during(NEGATIVE_OBSERVATION)
                .atMost(NEGATIVE_OBSERVATION.plusSeconds(1))
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertMonthlyWorkload(expectedMinutes));
    }

    private RegisteredUser registerTrainer(CreateTrainerRequest request) {
        return extractRegisteredUser(gymCoreClient.registerTrainer(request));
    }

    private RegisteredUser registerTrainee(CreateTraineeRequest request) {
        return extractRegisteredUser(gymCoreClient.registerTrainee(request));
    }

    private RegisteredUser extractRegisteredUser(Response response) {
        Map<String, String> registration = response
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .as(new TypeRef<>() {});
        return new RegisteredUser(registration.get("username"), registration.get("password"));
    }

    private Response createTraining(String traineeUsername, int durationMinutes) {
        TrainingRequest request = TrainingRequest.valid(traineeUsername,
                trainer.username(),
                "Core Workload Integration Training",
                trainingDate,
                durationMinutes);
        return gymCoreClient.createTraining(request.body(), APPLICATION_JSON, trainerToken);
    }

    private void awaitMonthlyWorkload(int expectedMinutes) {
        await()
                .alias("trainer monthly workload becomes " + expectedMinutes + " minutes")
                .atMost(UPDATE_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> assertMonthlyWorkload(expectedMinutes));
    }

    private void assertMonthlyWorkload(int expectedMinutes) {
        Response workloadResponse = workloadClient.getMonthlyWorkload(trainer.username(),
                trainingDate.getYear(),
                trainingDate.getMonth().name(),
                trainerToken);
        assertThat(workloadResponse.statusCode()).isEqualTo(HTTP_OK);
        assertThat(workloadResponse.jsonPath().getInt("trainingWorkingHours")).isEqualTo(expectedMinutes);
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, UNIQUE_SUFFIX_LENGTH);
    }

    private record RegisteredUser(String username, String password) {
    }

}
