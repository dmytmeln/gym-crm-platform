package com.gym.crm.core.security;

import com.gia.openapi.model.ErrorResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.config.MySqlContainerTestConfig;
import com.gym.crm.core.config.RedisContainerTestConfig;
import com.gym.crm.core.config.TestDataset;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.security.JwtService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gym.crm.core.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gym.crm.core.exception.ApiError.IP_BLOCKED_ERROR;
import static com.gym.crm.core.exception.ApiError.USER_DEACTIVATED_ERROR;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestDataset
class JwtAuthenticationIntegrationTest {

    private static final String BASE_PATH = "/api/v1";
    private static final String LOGIN_ENDPOINT = BASE_PATH + "/auth/login";
    private static final String LOGOUT_ENDPOINT = BASE_PATH + "/auth/logout";
    private static final String PASSWORD_ENDPOINT = BASE_PATH + "/auth/password";
    private static final String TRAININGS_ENDPOINT = BASE_PATH + "/trainings";
    private static final String TRAINING_TYPES_ENDPOINT = TRAININGS_ENDPOINT + "/types";
    private static final String TRAINEE_RESOURCE_ENDPOINT = BASE_PATH + "/trainees/{username}";
    private static final String TRAINER_RESOURCE_ENDPOINT = BASE_PATH + "/trainers/{username}";
    private static final String EXISTING_TRAINEE_USERNAME = "liam.miller";
    private static final String EXISTING_TRAINEE_PASSWORD = "password123";
    private static final String EXISTING_TRAINER_USERNAME = "marcus.stone";

    private static ErrorResponse authenticationErrorResponse;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void setIntegrationTestProperties(DynamicPropertyRegistry registry) {
        MySqlContainerTestConfig.setMySqlContainerProperties(registry);
        RedisContainerTestConfig.setRedisContainerProperties(registry);
    }

    @BeforeAll
    static void setUp() {
        authenticationErrorResponse = new ErrorResponse(AUTHENTICATION_ERROR.getCode(), AUTHENTICATION_ERROR.getMessage());
    }

    @BeforeEach
    void cleanRedis() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys.isEmpty()) {
            return;
        }

        redisTemplate.delete(keys);
    }

    @Test
    void shouldAuthenticateUserWhenCredentialsAreValid() {
        RequestEntity<LoginRequest> request = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, EXISTING_TRAINEE_PASSWORD));

        ResponseEntity<Void> actual = restTemplate.exchange(request, Void.class);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(actual.getHeaders().get(AUTHORIZATION))
                .isNotNull()
                .hasSize(1);
        String bearerAuthToken = actual.getHeaders().get(AUTHORIZATION).getFirst();
        assertThat(bearerAuthToken)
                .isNotNull()
                .startsWith("Bearer ");
        assertThat(bearerAuthToken.substring(7)).isNotEmpty();
    }

    @Test
    void shouldNotAccessTraineeProtectedEndpointWithoutToken() {
        RequestEntity<Void> request = RequestEntity
                .get(TRAINEE_RESOURCE_ENDPOINT, EXISTING_TRAINEE_USERNAME)
                .build();

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(authenticationErrorResponse);
        assertThat(actual.getHeaders().get(WWW_AUTHENTICATE))
                .isNotNull()
                .hasSize(1)
                .containsExactly("Bearer");
    }

    @Test
    void shouldNotAccessTrainerProtectedEndpointWithoutToken() {
        RequestEntity<Void> request = RequestEntity
                .get(TRAINER_RESOURCE_ENDPOINT, EXISTING_TRAINER_USERNAME)
                .build();

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(authenticationErrorResponse);
        assertThat(actual.getHeaders().get(WWW_AUTHENTICATE))
                .isNotNull()
                .hasSize(1)
                .containsExactly("Bearer");
    }

    @Test
    void shouldNotAccessTrainingProtectedEndpointWithoutToken() {
        RequestEntity<Void> request = RequestEntity
                .get(TRAINING_TYPES_ENDPOINT)
                .build();

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(authenticationErrorResponse);
        assertThat(actual.getHeaders().get(WWW_AUTHENTICATE))
                .isNotNull()
                .hasSize(1)
                .containsExactly("Bearer");
    }

    @Test
    void shouldAccessTraineeProtectedEndpointWithValidToken() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINEE_RESOURCE_ENDPOINT, EXISTING_TRAINEE_USERNAME)
                .headers(headers)
                .build();
        TraineeGetResponse expectedTrainee = new TraineeGetResponse()
                .firstName("Liam")
                .lastName("Miller")
                .isActive(true)
                .dateOfBirth(LocalDate.of(1990, MAY, 15))
                .address("NYC");

        ResponseEntity<TraineeGetResponse> actual = restTemplate.exchange(request, TraineeGetResponse.class);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(actual.getBody())
                .usingRecursiveComparison()
                .ignoringFields("trainers")
                .isEqualTo(expectedTrainee);
    }

    @Test
    void shouldReturn401WhenTokenIsBlacklisted() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        RequestEntity<Void> logoutRequest = RequestEntity
                .post(LOGOUT_ENDPOINT)
                .headers(headers)
                .build();
        ResponseEntity<Void> logoutResponse = restTemplate.exchange(logoutRequest, Void.class);
        assertThat(logoutResponse.getStatusCode().is2xxSuccessful()).isTrue();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINEE_RESOURCE_ENDPOINT, EXISTING_TRAINEE_USERNAME)
                .headers(headers)
                .build();

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(authenticationErrorResponse);
        assertThat(actual.getHeaders().get(WWW_AUTHENTICATE))
                .isNotNull()
                .hasSize(1)
                .containsExactly("Bearer");
    }

    @Test
    void shouldFailAccessTraineeProfileWhenRequestingOtherTrainee() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINEE_RESOURCE_ENDPOINT, "sophia.wilson")
                .headers(headers)
                .build();
        ErrorResponse expectedError = new ErrorResponse(AUTHORIZATION_ERROR.getCode(), AUTHORIZATION_ERROR.getMessage());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHORIZATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldAccessTrainerProtectedEndpointWithValidToken() {
        HttpHeaders headers = createBearerAuthHeadersForTrainer();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINER_RESOURCE_ENDPOINT, EXISTING_TRAINER_USERNAME)
                .headers(headers)
                .build();
        TrainerGetResponse expectedTrainer = new TrainerGetResponse()
                .firstName("Marcus")
                .lastName("Stone")
                .isActive(true)
                .specialization("CARDIO");

        ResponseEntity<TrainerGetResponse> actual = restTemplate.exchange(request, TrainerGetResponse.class);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(actual.getBody())
                .usingRecursiveComparison()
                .ignoringFields("trainees")
                .isEqualTo(expectedTrainer);
    }

    @Test
    void shouldFailAccessTrainerProfileWhenRequestingOtherTrainer() {
        HttpHeaders headers = createBearerAuthHeadersForTrainer();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINER_RESOURCE_ENDPOINT, "sophia.wilson")
                .headers(headers)
                .build();
        ErrorResponse expectedError = new ErrorResponse(AUTHORIZATION_ERROR.getCode(), AUTHORIZATION_ERROR.getMessage());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHORIZATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldAccessTrainingProtectedEndpointWithValidToken() {
        HttpHeaders headers = createBearerAuthHeadersForTrainer();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINING_TYPES_ENDPOINT)
                .headers(headers)
                .build();
        List<TrainingTypeResponse> expectedTrainingTypes = List.of(new TrainingTypeResponse().id(1).name("CARDIO"),
                new TrainingTypeResponse().id(2).name("STRENGTH"),
                new TrainingTypeResponse().id(3).name("YOGA"));

        ResponseEntity<List<TrainingTypeResponse>> actual = restTemplate.exchange(request, new ParameterizedTypeReference<>() {});

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(actual.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedTrainingTypes);
    }

    @Test
    void shouldFailAccessTrainingCreationWhenRequestingAsTrainee() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        TrainingCreateRequest trainingCreateRequest = new TrainingCreateRequest()
                .trainerUsername(EXISTING_TRAINER_USERNAME)
                .traineeUsername(EXISTING_TRAINEE_USERNAME)
                .trainingName("dummy")
                .trainingDate(LocalDate.of(2026, MAY, 15))
                .trainingDuration(10);
        RequestEntity<TrainingCreateRequest> request = RequestEntity
                .post(TRAININGS_ENDPOINT)
                .headers(headers)
                .body(trainingCreateRequest);
        ErrorResponse expectedError = new ErrorResponse(AUTHORIZATION_ERROR.getCode(), AUTHORIZATION_ERROR.getMessage());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHORIZATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldChangePasswordAndReturn401ForLoginWithOldPassword() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        RequestEntity<LoginChangeRequest> request = RequestEntity
                .put(PASSWORD_ENDPOINT)
                .headers(headers)
                .body(new LoginChangeRequest(EXISTING_TRAINEE_USERNAME, EXISTING_TRAINEE_PASSWORD, "newPassword"));

        ResponseEntity<Void> actual = restTemplate.exchange(request, Void.class);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        RequestEntity<LoginRequest> loginRequest = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, EXISTING_TRAINEE_PASSWORD));
        ResponseEntity<Void> loginResponse = restTemplate.exchange(loginRequest, Void.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        assertThat(actual.getHeaders().get(WWW_AUTHENTICATE)).isNull();
    }

    @Test
    void shouldChangePasswordAndReturn200ForLoginWithNewPassword() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        String newPassword = "newPassword";
        RequestEntity<LoginChangeRequest> request = RequestEntity
                .put(PASSWORD_ENDPOINT)
                .headers(headers)
                .body(new LoginChangeRequest(EXISTING_TRAINEE_USERNAME, EXISTING_TRAINEE_PASSWORD, newPassword));

        ResponseEntity<Void> actual = restTemplate.exchange(request, Void.class);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        RequestEntity<LoginRequest> loginRequest = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, newPassword));
        ResponseEntity<Void> loginResponse = restTemplate.exchange(loginRequest, Void.class);
        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResponse.getHeaders().get(AUTHORIZATION))
                .isNotNull()
                .hasSize(1);
        String bearerAuthToken = loginResponse.getHeaders().get(AUTHORIZATION).getFirst();
        assertThat(bearerAuthToken)
                .isNotNull()
                .startsWith("Bearer ");
        assertThat(bearerAuthToken.substring(7)).isNotEmpty();
    }

    @Test
    void shouldFailChangePasswordWhenRequestingOtherUser() {
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        String otherUserUsername = "sophia.wilson";
        RequestEntity<LoginChangeRequest> request = RequestEntity
                .put(PASSWORD_ENDPOINT)
                .headers(headers)
                .body(new LoginChangeRequest(otherUserUsername, EXISTING_TRAINEE_PASSWORD, "newPassword"));
        ErrorResponse expectedError = new ErrorResponse(AUTHORIZATION_ERROR.getCode(), AUTHORIZATION_ERROR.getMessage());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHORIZATION_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldReturn403ForDeactivatedUser() {
        User user = userRepository.findByUsername(EXISTING_TRAINEE_USERNAME).orElseThrow();
        userRepository.save(user.toBuilder().isActive(false).build());
        HttpHeaders headers = createBearerAuthHeadersForTrainee();
        RequestEntity<Void> request = RequestEntity
                .get(TRAINEE_RESOURCE_ENDPOINT, EXISTING_TRAINEE_USERNAME)
                .headers(headers)
                .build();
        ErrorResponse expectedError = new ErrorResponse(USER_DEACTIVATED_ERROR.getCode(), USER_DEACTIVATED_ERROR.getMessage());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(request, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(USER_DEACTIVATED_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldBlockIpAfterThreeFailedAttempts() {
        RequestEntity<LoginRequest> invalidRequest = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, "wrongPassword"));
        ErrorResponse expectedError = new ErrorResponse(IP_BLOCKED_ERROR.getCode(), IP_BLOCKED_ERROR.getMessage());
        ResponseEntity<ErrorResponse> failureResponse1 = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse1.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        ResponseEntity<ErrorResponse> failureResponse2 = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse2.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        ResponseEntity<ErrorResponse> failureResponse = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(invalidRequest, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(IP_BLOCKED_ERROR.getStatus());
        assertThat(actual.getBody()).isEqualTo(expectedError);
    }

    @Test
    void shouldResetCounterAfterSuccessfulLogin() {
        RequestEntity<LoginRequest> invalidRequest = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, "wrongPassword"));
        RequestEntity<LoginRequest> validRequest = RequestEntity
                .post(LOGIN_ENDPOINT)
                .body(new LoginRequest(EXISTING_TRAINEE_USERNAME, EXISTING_TRAINEE_PASSWORD));
        ResponseEntity<ErrorResponse> failureResponse1 = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse1.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        ResponseEntity<ErrorResponse> failureResponse2 = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse2.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
        ResponseEntity<Void> successResponse = restTemplate.exchange(validRequest, Void.class);
        assertThat(successResponse.getStatusCode().is2xxSuccessful()).isTrue();
        ResponseEntity<ErrorResponse> failureResponse3 = restTemplate.exchange(invalidRequest, ErrorResponse.class);
        assertThat(failureResponse3.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());

        ResponseEntity<ErrorResponse> actual = restTemplate.exchange(invalidRequest, ErrorResponse.class);

        assertThat(actual.getStatusCode()).isEqualTo(AUTHENTICATION_ERROR.getStatus());
    }

    private HttpHeaders createBearerAuthHeadersForTrainee() {
        String token = jwtService.generateAccessToken(EXISTING_TRAINEE_USERNAME);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return headers;
    }

    private HttpHeaders createBearerAuthHeadersForTrainer() {
        String token = jwtService.generateAccessToken(EXISTING_TRAINER_USERNAME);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return headers;
    }

}
