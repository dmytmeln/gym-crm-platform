package com.gym.crm.core.facade;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.ValidationException;
import com.gym.crm.core.mapper.AuthMapper;
import com.gym.crm.core.mapper.TraineeMapper;
import com.gym.crm.core.mapper.TrainerMapper;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.service.AuthenticationService;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.common.BusinessValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gym.crm.core.entity.EntityType.TRAINEE;
import static com.gym.crm.core.entity.EntityType.TRAINER;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_ADDRESS;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_DATE_OF_BIRTH;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_FIRST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_LAST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_PASSWORD;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_TRAINEE_ID;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithId;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithIdAndUserId;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_SPECIALIZATION;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_TRAINER_ID;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithId;
import static com.gym.crm.core.factory.TrainingTestFactory.DEFAULT_TRAINING_ID;
import static com.gym.crm.core.factory.TrainingTestFactory.buildTrainingWithId;
import static java.time.Month.JULY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final String USERNAME = "username";
    private static final String USERNAME_NULL_MSG = "Username cannot be null";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private BusinessValidator businessValidator;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private GymFacade facade;

    @Test
    void shouldCreateTraineeAndReturnResponse() {
        TraineeCreateRequest request = new TraineeCreateRequest(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)
                .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
                .address(DEFAULT_ADDRESS);
        Trainee trainee = buildTraineeWithId(DEFAULT_TRAINEE_ID);
        TraineeCreateResponse expected = new TraineeCreateResponse(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        when(traineeMapper.toEntity(request)).thenReturn(trainee);
        when(traineeService.createTrainee(trainee)).thenReturn(trainee);
        when(traineeMapper.toCreateResponse(trainee)).thenReturn(expected);

        TraineeCreateResponse actual = facade.createTrainee(request);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(traineeMapper).toEntity(request);
        verify(traineeService).createTrainee(trainee);
        verify(traineeMapper).toCreateResponse(trainee);
    }

    @Test
    void shouldThrowNullPointerWhenCreatingNullTraineeRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.createTrainee(null));

        assertEquals("TraineeCreateRequest cannot be null", exception.getMessage());

        verifyNoInteractions(traineeMapper, traineeService);
    }

    @Test
    void shouldGetTraineeByUsernameAndMapToGetResponse() {
        String username = "username";
        Trainee trainee = buildTraineeWithIdAndUserId();
        TraineeGetResponse expected = new TraineeGetResponse(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)
                .address(DEFAULT_ADDRESS)
                .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
                .isActive(true);

        when(traineeService.getTraineeByUsername(username)).thenReturn(trainee);
        when(traineeMapper.toGetResponse(trainee)).thenReturn(expected);

        TraineeGetResponse actual = facade.getTraineeByUsername(username);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(traineeService).getTraineeByUsername(username);
        verify(traineeMapper).toGetResponse(trainee);
    }

    @Test
    void shouldPropagateEntityNotFoundExceptionWhenGettingTraineeByUsername() {
        String username = "username";
        when(traineeService.getTraineeByUsername(username)).thenThrow(EntityNotFoundException.forUsername(TRAINEE, username));

        assertThrows(EntityNotFoundException.class, () -> facade.getTraineeByUsername(username));

        verify(traineeService).getTraineeByUsername(username);
        verifyNoInteractions(traineeMapper);
    }

    @Test
    void shouldThrowNullPointerWhenGettingTraineeByNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.getTraineeByUsername(null));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(traineeService, traineeMapper);
    }

    @Test
    void shouldGetTraineeTrainingsAndMapToResponseList() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder().build();
        Training training = buildTrainingWithId(DEFAULT_TRAINING_ID);
        GetTraineeTrainingResponse responseDto = new GetTraineeTrainingResponse();
        List<Training> trainings = List.of(training);
        List<GetTraineeTrainingResponse> expected = List.of(responseDto);

        doNothing().when(businessValidator).validate(filter);
        when(traineeService.getTraineeTrainings(filter)).thenReturn(trainings);
        when(traineeMapper.toGetTraineeTrainingResponseList(trainings)).thenReturn(expected);

        List<GetTraineeTrainingResponse> actual = facade.getTraineeTrainings(filter);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(businessValidator).validate(filter);
        verify(traineeService).getTraineeTrainings(filter);
        verify(traineeMapper).toGetTraineeTrainingResponseList(trainings);
    }

    @Test
    void shouldThrowNullPointerWhenGettingTraineeTrainingsWithNullFilter() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.getTraineeTrainings(null));

        assertEquals("Filter cannot be null", exception.getMessage());
        verifyNoInteractions(businessValidator, traineeService, traineeMapper);
    }

    @Test
    void shouldGetAvailableTrainersForTraineeAndMapToResponseList() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        AssignedTrainerResponse responseDto = new AssignedTrainerResponse();
        List<Trainer> trainers = List.of(trainer);
        List<AssignedTrainerResponse> expected = List.of(responseDto);

        when(traineeService.getAvailableTrainers(USERNAME)).thenReturn(trainers);
        when(traineeMapper.toAssignedTrainerResponseListFromList(trainers)).thenReturn(expected);

        List<AssignedTrainerResponse> actual = facade.getAvailableTrainersForTrainee(USERNAME);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(traineeService).getAvailableTrainers(USERNAME);
        verify(traineeMapper).toAssignedTrainerResponseListFromList(trainers);
    }

    @Test
    void shouldThrowNullPointerWhenGettingAvailableTrainersWithNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> facade.getAvailableTrainersForTrainee(null));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(traineeService, traineeMapper);
    }

    @Test
    void shouldUpdateTraineeAndMapToUpdateResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest("Sophia", "Wilson", false)
                .dateOfBirth(DEFAULT_DATE_OF_BIRTH)
                .address("456 Oak Ave");
        Trainee trainee = buildTraineeWithId(DEFAULT_TRAINEE_ID);
        TraineeUpdateResponse expected = new TraineeUpdateResponse("Sophia", "Wilson")
                .username(USERNAME)
                .isActive(false);

        when(traineeMapper.toEntity(request, USERNAME)).thenReturn(trainee);
        when(traineeService.updateTrainee(trainee)).thenReturn(trainee);
        when(traineeMapper.toUpdateResponse(trainee)).thenReturn(expected);

        TraineeUpdateResponse actual = facade.updateTrainee(USERNAME, request);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(traineeMapper).toEntity(request, USERNAME);
        verify(traineeService).updateTrainee(trainee);
        verify(traineeMapper).toUpdateResponse(trainee);
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingWithNullTraineeRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.updateTrainee(USERNAME, null));

        assertEquals("TraineeUpdateRequest cannot be null", exception.getMessage());

        verifyNoInteractions(traineeMapper, traineeService);
    }

    @Test
    void shouldUpdateTraineeTrainersAndMapToResponse() {
        List<String> trainerUsernames = List.of("trainer1", "trainer2");
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest().trainerUsernames(trainerUsernames);
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        AssignedTrainerResponse responseDto = new AssignedTrainerResponse();
        Set<Trainer> trainers = Set.of(trainer);
        List<AssignedTrainerResponse> expectedTrainers = List.of(responseDto);
        TraineeAssignedTrainersUpdateResponse expectedResponse = new TraineeAssignedTrainersUpdateResponse().trainers(expectedTrainers);
        Trainee updatedTrainee = Trainee.builder()
                .id(DEFAULT_TRAINEE_ID)
                .trainers(trainers)
                .build();

        when(traineeService.updateTraineeTrainers(USERNAME, trainerUsernames)).thenReturn(updatedTrainee);
        when(traineeMapper.toAssignedTrainersUpdateResponse(trainers)).thenReturn(expectedResponse);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainers(USERNAME, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(traineeService).updateTraineeTrainers(USERNAME, trainerUsernames);
        verify(traineeMapper).toAssignedTrainersUpdateResponse(trainers);
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingTraineeTrainersWithNullUsername() {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest().trainerUsernames(List.of("trainer1"));

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> facade.updateTraineeTrainers(null, request));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(traineeService, traineeMapper);
    }

    @Test
    void shouldUpdateTraineeActivationStatusToActive() {
        ActivationStatusRequest request = new ActivationStatusRequest(true);

        facade.updateTraineeActivationStatus(USERNAME, request);

        verify(traineeService).updateActivationStatus(USERNAME, true);
    }

    @Test
    void shouldUpdateTraineeActivationStatusToInactive() {
        ActivationStatusRequest request = new ActivationStatusRequest(false);

        facade.updateTraineeActivationStatus(USERNAME, request);

        verify(traineeService).updateActivationStatus(USERNAME, false);
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingActivationStatusWithNullUsername() {
        ActivationStatusRequest request = new ActivationStatusRequest(true);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> facade.updateTraineeActivationStatus(null, request));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(traineeService);
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        String username = "username";
        when(traineeService.deleteTraineeByUsername(username)).thenReturn(true);

        boolean actual = facade.deleteTraineeByUsername(username);

        assertTrue(actual);
        verify(traineeService).deleteTraineeByUsername(username);
    }

    @Test
    void shouldReturnFalseWhenDeletingTraineeByNonExistentUsername() {
        String username = "username";
        when(traineeService.deleteTraineeByUsername(username)).thenReturn(false);

        boolean actual = facade.deleteTraineeByUsername(username);

        assertFalse(actual);
        verify(traineeService).deleteTraineeByUsername(username);
    }

    @Test
    void shouldThrowNullPointerWhenDeletingNullTraineeUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.deleteTraineeByUsername(null));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(traineeService);
    }

    @Test
    void shouldChangePasswordForTrainee() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, "oldPassword123", "newPassword123");
        LoginChangeDto dto = new LoginChangeDto(USERNAME, "oldPassword123", "newPassword123");

        doNothing().when(businessValidator).validate(request);
        doNothing().when(businessValidator).validate(dto);
        when(authMapper.toDto(request)).thenReturn(dto);

        facade.changePassword(request);

        verify(authMapper).toDto(request);
        verify(businessValidator).validate(request);
        verify(businessValidator).validate(dto);
        verify(authenticationService).changePassword(dto);
    }

    @Test
    void shouldThrowValidationExceptionWhenChangingPasswordWithInvalidDto() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, "oldPassword", "newPassword");
        LoginChangeDto dto = new LoginChangeDto(USERNAME, "oldPassword", "newPassword");

        doNothing().when(businessValidator).validate(request);
        when(authMapper.toDto(request)).thenReturn(dto);
        doThrow(new ValidationException("Validation error")).when(businessValidator).validate(dto);

        assertThrows(ValidationException.class, () -> facade.changePassword(request));

        verify(businessValidator).validate(request);
        verify(businessValidator).validate(dto);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldThrowNullPointerWhenChangingPasswordWithNullRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.changePassword(null));

        assertEquals("LoginChangeRequest cannot be null", exception.getMessage());
        verifyNoInteractions(authMapper, businessValidator, authenticationService);
    }

    @Test
    void shouldLoginAndReturnToken() {
        LoginRequest request = new LoginRequest().username(USERNAME).password("password123");
        LoginRequestDto dto = new LoginRequestDto(USERNAME, "password123");
        String expectedToken = "mocked-jwt-token";

        doNothing().when(businessValidator).validate(request);
        doNothing().when(businessValidator).validate(dto);
        when(authMapper.toDto(request)).thenReturn(dto);
        when(authenticationService.login(dto)).thenReturn(expectedToken);

        String actual = facade.login(request);

        assertEquals(expectedToken, actual);
        verify(businessValidator).validate(request);
        verify(authMapper).toDto(request);
        verify(businessValidator).validate(dto);
        verify(authenticationService).login(dto);
    }

    @Test
    void shouldLogoutSuccessfully() {
        String token = "valid-token";

        facade.logout(token);

        verify(authenticationService).logout(token);
    }

    @Test
    void shouldThrowNullPointerWhenLoggingOutWithNullToken() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.logout(null));

        assertEquals("Token cannot be null", exception.getMessage());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldCreateTrainerAndReturnResponse() {
        TrainerCreateRequest request = new TrainerCreateRequest()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .specialization(DEFAULT_SPECIALIZATION);
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        TrainerCreateResponse expected = new TrainerCreateResponse()
                .username(USERNAME)
                .password(DEFAULT_PASSWORD);

        doNothing().when(businessValidator).validate(request);
        when(trainerMapper.toEntity(request)).thenReturn(trainer);
        when(trainerService.createTrainer(trainer)).thenReturn(trainer);
        when(trainerMapper.toCreateResponse(trainer)).thenReturn(expected);

        TrainerCreateResponse actual = facade.createTrainer(request);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(businessValidator).validate(request);
        verify(trainerMapper).toEntity(request);
        verify(trainerService).createTrainer(trainer);
        verify(trainerMapper).toCreateResponse(trainer);
    }

    @Test
    void shouldThrowValidationExceptionWhenRegisteringTrainerWithInvalidRequest() {
        TrainerCreateRequest request = new TrainerCreateRequest()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .specialization(DEFAULT_SPECIALIZATION);

        doThrow(new ValidationException("Validation error")).when(businessValidator).validate(request);

        assertThrows(ValidationException.class, () -> facade.createTrainer(request));

        verify(businessValidator).validate(request);
        verifyNoInteractions(trainerMapper, trainerService);
    }

    @Test
    void shouldThrowNullPointerWhenRegisteringNullTrainerRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.createTrainer(null));

        assertEquals("TrainerCreateRequest cannot be null", exception.getMessage());

        verifyNoInteractions(businessValidator, trainerMapper, trainerService);
    }

    @Test
    void shouldGetTrainerProfileAndMapToGetResponse() {
        String username = "username";
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        TrainerGetResponse expected = new TrainerGetResponse()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .specialization(DEFAULT_SPECIALIZATION)
                .isActive(true);

        when(trainerService.getTrainerByUsername(username)).thenReturn(trainer);
        when(trainerMapper.toGetResponse(trainer)).thenReturn(expected);

        TrainerGetResponse actual = facade.getTrainerByUsername(username);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(trainerService).getTrainerByUsername(username);
        verify(trainerMapper).toGetResponse(trainer);
    }

    @Test
    void shouldPropagateEntityNotFoundExceptionWhenGettingTrainerProfile() {
        String username = "username";
        when(trainerService.getTrainerByUsername(username)).thenThrow(EntityNotFoundException.forUsername(TRAINER, username));

        assertThrows(EntityNotFoundException.class, () -> facade.getTrainerByUsername(username));

        verify(trainerService).getTrainerByUsername(username);
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void shouldThrowNullPointerWhenGettingTrainerProfileByNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.getTrainerByUsername(null));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainerService, trainerMapper);
    }

    @Test
    void shouldUpdateTrainerProfileAndMapToUpdateResponse() {
        TrainerUpdateRequest request = new TrainerUpdateRequest()
                .firstName("Elena")
                .lastName("Rodriguez")
                .isActive(false);
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        TrainerUpdateResponse expected = new TrainerUpdateResponse()
                .username(USERNAME)
                .firstName("Elena")
                .lastName("Rodriguez")
                .specialization(DEFAULT_SPECIALIZATION)
                .isActive(false);

        doNothing().when(businessValidator).validate(request);
        when(trainerMapper.toEntity(request, USERNAME)).thenReturn(trainer);
        when(trainerService.updateTrainer(trainer)).thenReturn(trainer);
        when(trainerMapper.toUpdateResponse(trainer)).thenReturn(expected);

        TrainerUpdateResponse actual = facade.updateTrainer(USERNAME, request);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(businessValidator).validate(request);
        verify(trainerMapper).toEntity(request, USERNAME);
        verify(trainerService).updateTrainer(trainer);
        verify(trainerMapper).toUpdateResponse(trainer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdatingTrainerProfileWithInvalidRequest() {
        TrainerUpdateRequest request = new TrainerUpdateRequest()
                .firstName("Elena")
                .lastName("Rodriguez")
                .isActive(false);

        doThrow(new ValidationException("Validation error")).when(businessValidator).validate(request);

        assertThrows(ValidationException.class, () -> facade.updateTrainer(USERNAME, request));

        verify(businessValidator).validate(request);
        verifyNoInteractions(trainerMapper, trainerService);
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingWithNullTrainerRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.updateTrainer(USERNAME, null));

        assertEquals("TrainerUpdateRequest cannot be null", exception.getMessage());

        verifyNoInteractions(businessValidator, trainerMapper, trainerService);
    }

    @Test
    void shouldUpdateTrainerActivationStatusToActive() {
        facade.updateTrainerActivationStatus(USERNAME, true);

        verify(trainerService).updateActivationStatus(USERNAME, true);
    }

    @Test
    void shouldUpdateTrainerActivationStatusToInactive() {
        facade.updateTrainerActivationStatus(USERNAME, false);

        verify(trainerService).updateActivationStatus(USERNAME, false);
    }

    @Test
    void shouldThrowNullPointerWhenChangingTrainerActivationStatusWithNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.updateTrainerActivationStatus(null, true));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainerService);
    }

    @Test
    void shouldGetTrainerTrainings() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(USERNAME)
                .fromDate(LocalDate.of(2025, JULY, 1))
                .toDate(LocalDate.of(2025, JULY, 31))
                .traineeName("Liam Miller")
                .build();
        Training training = buildTrainingWithId(DEFAULT_TRAINING_ID);
        GetTrainerTrainingResponse response = new GetTrainerTrainingResponse()
                .trainingName("Morning Cardio")
                .trainingType("Cardio")
                .traineeName("liam.miller")
                .trainingDate(LocalDate.of(2025, JULY, 20))
                .trainingDuration(55);
        List<Training> trainings = List.of(training);
        List<GetTrainerTrainingResponse> expected = List.of(response);

        doNothing().when(businessValidator).validate(filter);
        when(trainerService.getTrainerTrainings(filter)).thenReturn(trainings);
        when(trainerMapper.toGetTrainerTrainingResponseList(trainings)).thenReturn(expected);

        List<GetTrainerTrainingResponse> actual = facade.getTrainerTrainings(filter);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(businessValidator).validate(filter);
        verify(trainerService).getTrainerTrainings(filter);
        verify(trainerMapper).toGetTrainerTrainingResponseList(trainings);
    }

    @Test
    void shouldThrowNullPointerWhenTrainerFilterIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.getTrainerTrainings(null));

        assertEquals("Filter cannot be null", exception.getMessage());
        verifyNoInteractions(businessValidator, trainerService, trainerMapper);
    }

    @Test
    void shouldCreateTrainingWhenRequestIsValid() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        Training training = buildTrainingFromRequest(request);

        doNothing().when(businessValidator).validate(request);
        when(trainingMapper.toEntity(request)).thenReturn(training);
        when(trainingService.createTraining(training)).thenReturn(training);

        facade.createTraining(request);

        verify(businessValidator).validate(request);
        verify(trainingMapper).toEntity(request);
        verify(trainingService).createTraining(training);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingTrainingWithInvalidRequest() {
        TrainingCreateRequest request = buildTrainingCreateRequest();

        doThrow(new ValidationException("Validation error")).when(businessValidator).validate(request);

        assertThrows(ValidationException.class, () -> facade.createTraining(request));

        verify(businessValidator).validate(request);
        verifyNoInteractions(trainingMapper, trainingService);
    }

    @Test
    void shouldThrowNullPointerWhenCreatingNullTrainingRequest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> facade.createTraining(null));

        assertEquals("TrainingCreateRequest cannot be null", exception.getMessage());
        verifyNoInteractions(trainingMapper, trainingService);
    }

    @Test
    void shouldGetAllTrainingTypesAndMapToResponseList() {
        TrainingType trainingType = TrainingType.builder().id(1L).trainingTypeName("Cardio").build();
        TrainingTypeResponse expected = new TrainingTypeResponse().id(1).name("Cardio");
        List<TrainingType> trainingTypes = List.of(trainingType);
        List<TrainingTypeResponse> expectedList = List.of(expected);

        when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
        when(trainingMapper.toTrainingTypeResponseList(trainingTypes)).thenReturn(expectedList);

        List<TrainingTypeResponse> actual = facade.getAllTrainingTypes();

        assertNotNull(actual);
        assertEquals(expectedList, actual);
        verify(trainingService).getAllTrainingTypes();
        verify(trainingMapper).toTrainingTypeResponseList(trainingTypes);
    }

    @Test
    void shouldGetAllTrainingTypesAndReturnEmptyList() {
        when(trainingService.getAllTrainingTypes()).thenReturn(List.of());
        when(trainingMapper.toTrainingTypeResponseList(List.of())).thenReturn(List.of());

        List<TrainingTypeResponse> actual = facade.getAllTrainingTypes();

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(trainingService).getAllTrainingTypes();
        verify(trainingMapper).toTrainingTypeResponseList(List.of());
    }

    private TrainingCreateRequest buildTrainingCreateRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername(DEFAULT_USERNAME);
        request.setTrainerUsername("trainer.test");
        request.setTrainingName("Morning Cardio");
        request.setTrainingDate(LocalDate.of(2025, JULY, 20));
        request.setTrainingDuration(55);

        return request;
    }

    private Training buildTrainingFromRequest(TrainingCreateRequest request) {
        return Training.builder()
                .trainingName(request.getTrainingName())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();
    }

}
