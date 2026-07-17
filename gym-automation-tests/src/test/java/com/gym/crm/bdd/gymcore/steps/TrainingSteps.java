package com.gym.crm.bdd.gymcore.steps;

import com.gym.crm.bdd.gymcore.client.GymCoreClient;
import com.gym.crm.bdd.gymcore.client.TrainerWorkloadQueueConsumer;
import com.gym.crm.bdd.gymcore.client.TrainerWorkloadQueueConsumer.ReceivedWorkload;
import com.gym.crm.bdd.gymcore.model.CreateTraineeRequest;
import com.gym.crm.bdd.gymcore.model.CreateTrainerRequest;
import com.gym.crm.bdd.gymcore.model.TrainingRequest;
import com.gym.crm.bdd.gymcore.support.GymCoreComponentStack;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSteps {

    private static final int UNIQUE_SUFFIX_LENGTH = 8;
    private static final int WORKLOAD_WAIT_SECONDS = 5;
    private static final int HTTP_OK = 200;
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BEARER_CHALLENGE = "Bearer";
    private static final String QUEUE_PREFIX = "queue://";

    private final GymCoreClient gymCoreClient = new GymCoreClient();
    private final TrainerWorkloadQueueConsumer workloadQueueConsumer = new TrainerWorkloadQueueConsumer();

    private RegisteredTrainer trainer;
    private RegisteredTrainee trainee;
    private TrainingRequest trainingRequest;
    private String accessToken;
    private Response response;

    @Given("trainer is created with:")
    public void trainerIsCreatedWith(CreateTrainerRequest request) {
        Response registrationResponse = gymCoreClient.registerTrainer(request);
        Map<String, String> trainerRegistration = registrationResponse.then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .as(new TypeRef<>() {});
        trainer = new RegisteredTrainer(trainerRegistration.get("username"),
                trainerRegistration.get("password"),
                request.getFirstName(),
                request.getLastName());
    }

    @Given("trainee is created with:")
    public void traineeIsCreatedWith(CreateTraineeRequest request) {
        Response registrationResponse = gymCoreClient.registerTrainee(request);
        Map<String, String> traineeRegistration = registrationResponse.then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .as(new TypeRef<>() {});
        trainee = new RegisteredTrainee(traineeRegistration.get("username"), traineeRegistration.get("password"));
    }

    @Given("caller is trainer")
    public void callerIsTrainer() {
        accessToken = gymCoreClient.login(trainer.username(), trainer.password());
    }

    @Given("training request is created with name {string}, date offset {int} days and duration {int} minutes")
    public void trainingRequestIsCreated(String trainingName, int trainingDateOffsetDays, int trainingDuration) {
        trainingRequest = TrainingRequest.valid(trainee.username(),
                trainer.username(),
                trainingName,
                LocalDate.now().plusDays(trainingDateOffsetDays),
                trainingDuration);
    }

    @Given("caller is trainee")
    public void callerIsTrainee() {
        accessToken = gymCoreClient.login(trainee.username(), trainee.password());
    }

    @Given("trainer token was logged out")
    public void trainerTokenWasLoggedOut() {
        gymCoreClient.logout(accessToken);
    }

    @Given("trainer account is deactivated")
    public void trainerAccountIsDeactivated() {
        gymCoreClient.deactivateTrainer(trainer.username(), accessToken);
    }

    @Given("request names another trainer")
    public void requestNamesAnotherTrainer() {
        String suffix = uniqueSuffix();
        CreateTrainerRequest request = new CreateTrainerRequest("Other" + suffix, "Trainer", "CARDIO");
        Map<String, String> otherTrainer = gymCoreClient.registerTrainer(request)
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .as(new TypeRef<>() {});
        trainingRequest.useTrainer(otherTrainer.get("username"));
    }

    @Given("request names unknown trainee")
    public void requestNamesUnknownTrainee() {
        trainingRequest.useTrainee("unknown.trainee999999");
    }

    @Given("required field {string} is omitted")
    public void requiredFieldIsOmitted(String field) {
        trainingRequest.omitRequiredField(field);
    }

    @Given("training name is blank")
    public void trainingNameIsBlank() {
        trainingRequest.setName("");
    }

    @Given("training name is whitespace only")
    public void trainingNameIsWhitespaceOnly() {
        trainingRequest.setName("   ");
    }

    @Given("training name contains {int} characters")
    public void trainingNameContainsCharacters(int length) {
        trainingRequest.setName("x".repeat(length));
    }

    @Given("training duration is zero")
    public void trainingDurationIsZero() {
        trainingRequest.setDuration(0);
    }

    @Given("trainee username contains {int} characters")
    public void traineeUsernameContainsCharacters(int length) {
        trainingRequest.useTrainee("x".repeat(length));
    }

    @Given("training date is invalid")
    public void trainingDateIsInvalid() {
        trainingRequest.setDate("not-a-date");
    }

    @When("trainer creates Training")
    public void trainerCreatesTraining() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, accessToken);
    }

    @When("caller creates Training without token")
    public void callerCreatesWithoutToken() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, null);
    }

    @When("caller creates Training with invalid token")
    public void callerCreatesWithInvalidToken() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, accessToken + "invalid");
    }

    @When("caller sends malformed JSON")
    public void callerSendsMalformedJson() {
        response = gymCoreClient.createTraining("{broken", APPLICATION_JSON, accessToken);
    }

    @When("caller sends unsupported content type")
    public void callerSendsUnsupportedContentType() {
        response = gymCoreClient.createTraining("training", TEXT_PLAIN, accessToken);
    }

    @Then("Training creation succeeds")
    public void trainingCreationSucceeds() {
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
    }

    @Then("Training is visible through trainer schedule")
    public void trainingIsVisible() {
        JsonPath jsonPath = gymCoreClient.trainerTrainings(trainer.username(), accessToken)
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .jsonPath();
        assertThat(jsonPath.getList("")).hasSize(1);
        assertThat(jsonPath.getString("[0].trainingName")).isEqualTo(trainingRequest.name());
        assertThat(jsonPath.getString("[0].trainingDate")).isEqualTo(trainingRequest.date());
        assertThat(jsonPath.getInt("[0].trainingDuration")).isEqualTo(trainingRequest.duration());
        assertThat(jsonPath.getString("[0].trainingType")).isEqualTo("CARDIO");
        assertThat(jsonPath.getString("[0].traineeName")).isEqualTo(trainee.username());
    }

    @Then("exactly one matching ADD workload update is published")
    public void workloadUpdateIsPublished() throws Exception {
        List<ReceivedWorkload> messages = workloadQueueConsumer.awaitForUsername(trainer.username(),
                Duration.ofSeconds(WORKLOAD_WAIT_SECONDS));
        assertThat(messages).as("workload messages for trainer during observation window").hasSize(1);
        ReceivedWorkload message = messages.getFirst();
        assertThat(message.body().path("actionType").asText()).isEqualTo("ADD");
        assertThat(message.body().path("username").asText()).isEqualTo(trainer.username());
        assertThat(message.body().path("firstName").asText()).isEqualTo(trainer.firstName());
        assertThat(message.body().path("lastName").asText()).isEqualTo(trainer.lastName());
        assertThat(message.body().path("isActive").asBoolean()).isTrue();
        assertThat(message.body().path("trainingDate").asText()).isEqualTo(trainingRequest.date());
        assertThat(message.body().path("trainingDuration").asInt()).isEqualTo(trainingRequest.duration());
        assertThat(message.destination()).isEqualTo(QUEUE_PREFIX + GymCoreComponentStack.QUEUE);
    }

    @Then("request is rejected with status {int} and error code {int}")
    public void requestIsRejected(int status, int errorCode) {
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.jsonPath().getInt("errorCode")).isEqualTo(errorCode);
        assertThat(response.jsonPath().getString("errorMessage")).isNotBlank();
    }

    @Then("request is rejected with:")
    public void requestIsRejectedWith(ExpectedError expected) {
        assertThat(response.statusCode()).isEqualTo(expected.status());
        assertThat(response.jsonPath().getInt("errorCode")).isEqualTo(expected.errorCode());
        assertThat(response.jsonPath().getString("errorMessage")).isNotBlank();
    }

    @Then("response challenges Bearer authentication")
    public void responseChallengesBearerAuthentication() {
        assertThat(response.header(WWW_AUTHENTICATE_HEADER)).isEqualTo(BEARER_CHALLENGE);
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, UNIQUE_SUFFIX_LENGTH);
    }

    private record RegisteredTrainer(
            String username,
            String password,
            String firstName,
            String lastName
    ) {
    }

    private record RegisteredTrainee(String username, String password) {
    }

    public record ExpectedError(int status, int errorCode) {
    }

}
