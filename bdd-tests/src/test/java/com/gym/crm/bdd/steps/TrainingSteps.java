package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.GymCoreClient;
import com.gym.crm.bdd.client.WorkloadQueueClient;
import com.gym.crm.bdd.model.TrainingRequest;
import com.gym.crm.bdd.support.GymCoreComponentStack;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
    private static final int TRAINING_DATE_OFFSET_DAYS = 7;
    private static final int EXPECTED_TRAINING_DURATION_MINUTES = 60;
    private static final int WORKLOAD_WAIT_SECONDS = 5;
    private static final int HTTP_OK = 200;
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BEARER_CHALLENGE = "Bearer";
    private static final String QUEUE_PREFIX = "queue://";

    private final GymCoreClient gymCoreClient = new GymCoreClient();
    private final WorkloadQueueClient workloadQueueClient = new WorkloadQueueClient();
    private final LocalDate trainingDate = LocalDate.now().plusDays(TRAINING_DATE_OFFSET_DAYS);

    private RegisteredTrainer trainer;
    private RegisteredTrainee trainee;
    private TrainingRequest trainingRequest;
    private String accessToken;
    private Response response;

    @Given("an active trainer and trainee exist")
    public void activeTrainerAndTraineeExist() {
        String suffix = uniqueSuffix();
        String trainerFirstName = "Trainer" + suffix;
        String trainerLastName = "Component";

        Map<String, String> trainerRegistration = gymCoreClient.registerTrainer(trainerFirstName, trainerLastName);
        trainer = new RegisteredTrainer(trainerRegistration.get("username"),
                trainerRegistration.get("password"),
                trainerFirstName,
                trainerLastName);

        Map<String, String> traineeRegistration = gymCoreClient.registerTrainee("Trainee" + suffix, "Component");
        trainee = new RegisteredTrainee(traineeRegistration.get("username"), traineeRegistration.get("password"));

        accessToken = gymCoreClient.login(trainer.username(), trainer.password());
        trainingRequest = TrainingRequest.valid(trainee.username(),
                trainer.username(),
                trainingDate,
                EXPECTED_TRAINING_DURATION_MINUTES);
    }

    @Given("the caller is the trainee")
    public void callerIsTrainee() {
        accessToken = gymCoreClient.login(trainee.username(), trainee.password());
    }

    @Given("the trainer token was logged out")
    public void trainerTokenWasLoggedOut() {
        gymCoreClient.logout(accessToken);
    }

    @Given("the trainer account is deactivated")
    public void trainerAccountIsDeactivated() {
        gymCoreClient.deactivateTrainer(trainer.username(), accessToken);
    }

    @Given("the request names another trainer")
    public void requestNamesAnotherTrainer() {
        String suffix = uniqueSuffix();
        Map<String, String> otherTrainer = gymCoreClient.registerTrainer("Other" + suffix, "Trainer");
        trainingRequest.useTrainer(otherTrainer.get("username"));
    }

    @Given("the request names an unknown trainee")
    public void requestNamesUnknownTrainee() {
        trainingRequest.useTrainee("unknown.trainee999999");
    }

    @Given("the required field {string} is omitted")
    public void requiredFieldIsOmitted(String field) {
        trainingRequest.omitRequiredField(field);
    }

    @Given("the training name is blank")
    public void trainingNameIsBlank() {
        trainingRequest.setName("");
    }

    @Given("the training name is whitespace only")
    public void trainingNameIsWhitespaceOnly() {
        trainingRequest.setName("   ");
    }

    @Given("the training name contains {int} characters")
    public void trainingNameContainsCharacters(int length) {
        trainingRequest.setName("x".repeat(length));
    }

    @Given("the training duration is zero")
    public void trainingDurationIsZero() {
        trainingRequest.setDuration(0);
    }

    @Given("the trainee username contains {int} characters")
    public void traineeUsernameContainsCharacters(int length) {
        trainingRequest.useTrainee("x".repeat(length));
    }

    @Given("the training date is invalid")
    public void trainingDateIsInvalid() {
        trainingRequest.setDate("not-a-date");
    }

    @When("the trainer creates the Training")
    public void trainerCreatesTraining() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, accessToken);
    }

    @When("the caller creates the Training without a token")
    public void callerCreatesWithoutToken() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, null);
    }

    @When("the caller creates the Training with an invalid token")
    public void callerCreatesWithInvalidToken() {
        response = gymCoreClient.createTraining(trainingRequest.body(), APPLICATION_JSON, accessToken + "invalid");
    }

    @When("the caller sends malformed JSON")
    public void callerSendsMalformedJson() {
        response = gymCoreClient.createTraining("{broken", APPLICATION_JSON, accessToken);
    }

    @When("the caller sends an unsupported content type")
    public void callerSendsUnsupportedContentType() {
        response = gymCoreClient.createTraining("training", TEXT_PLAIN, accessToken);
    }

    @Then("Training creation succeeds")
    public void trainingCreationSucceeds() {
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
    }

    @Then("the Training is visible through the trainer schedule")
    public void trainingIsVisible() {
        JsonPath jsonPath = gymCoreClient.trainerTrainings(trainer.username(), accessToken)
                .then()
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .jsonPath();
        assertThat(jsonPath.getList("")).hasSize(1);
        assertThat(jsonPath.getString("[0].trainingName")).isEqualTo("Component Training");
        assertThat(jsonPath.getString("[0].trainingDate")).isEqualTo(trainingDate.toString());
        assertThat(jsonPath.getInt("[0].trainingDuration")).isEqualTo(EXPECTED_TRAINING_DURATION_MINUTES);
        assertThat(jsonPath.getString("[0].trainingType")).isEqualTo("CARDIO");
        assertThat(jsonPath.getString("[0].traineeName")).isEqualTo(trainee.username());
    }

    @Then("exactly one matching ADD workload update is published")
    public void workloadUpdateIsPublished() throws Exception {
        List<WorkloadQueueClient.ReceivedWorkload> messages = workloadQueueClient.awaitForUsername(trainer.username(),
                Duration.ofSeconds(WORKLOAD_WAIT_SECONDS));
        assertThat(messages).as("workload messages for trainer during observation window").hasSize(1);
        WorkloadQueueClient.ReceivedWorkload message = messages.getFirst();
        assertThat(message.body().path("actionType").asText()).isEqualTo("ADD");
        assertThat(message.body().path("username").asText()).isEqualTo(trainer.username());
        assertThat(message.body().path("firstName").asText()).isEqualTo(trainer.firstName());
        assertThat(message.body().path("lastName").asText()).isEqualTo(trainer.lastName());
        assertThat(message.body().path("isActive").asBoolean()).isTrue();
        assertThat(message.body().path("trainingDate").asText()).isEqualTo(trainingDate.toString());
        assertThat(message.body().path("trainingDuration").asInt()).isEqualTo(EXPECTED_TRAINING_DURATION_MINUTES);
        assertThat(message.destination()).isEqualTo(QUEUE_PREFIX + GymCoreComponentStack.QUEUE);
    }

    @Then("the request is rejected with status {int} and error code {int}")
    public void requestIsRejected(int status, int errorCode) {
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.jsonPath().getInt("errorCode")).isEqualTo(errorCode);
        assertThat(response.jsonPath().getString("errorMessage")).isNotBlank();
    }

    @Then("the response challenges Bearer authentication")
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

}
