package com.gym.crm.core.service.impl;

import com.gym.crm.core.client.WorkloadClientFacade;
import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.ConflictException;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.specification.TraineeTrainingCriteriaBuilder;
import com.gym.crm.core.service.common.ProfileCredentialGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_FIRST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_LAST_NAME;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_PASSWORD;
import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithId;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithUsername;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithoutCredentials;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithoutUser;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithId;
import static java.time.Month.MAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository repository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private ProfileCredentialGenerator generator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TraineeTrainingCriteriaBuilder trainingCriteriaBuilder;

    @Mock
    private WorkloadClientFacade workloadClientFacade;

    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void shouldCreateTraineeWithGeneratedCredentials() {
        Trainee traineeWithoutCredentials = buildTraineeWithoutCredentials();
        Trainee expected = buildTraineeWithId();
        String encodedPassword = "encoded-" + DEFAULT_PASSWORD;

        when(generator.generateUsername(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)).thenReturn(DEFAULT_USERNAME);
        when(generator.generatePassword()).thenReturn(DEFAULT_PASSWORD);
        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn(encodedPassword);
        when(repository.save(any(Trainee.class))).thenReturn(expected);

        Trainee actual = service.createTrainee(traineeWithoutCredentials);

        verify(generator).generateUsername(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);
        verify(generator).generatePassword();
        verify(passwordEncoder).encode(DEFAULT_PASSWORD);
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(repository).save(traineeCaptor.capture());
        Trainee traineeWithCredentials = traineeCaptor.getValue();
        assertEquals(DEFAULT_USERNAME, traineeWithCredentials.getUser().getUsername());
        assertEquals(encodedPassword, traineeWithCredentials.getUser().getPassword());
        assertEquals(DEFAULT_FIRST_NAME, traineeWithCredentials.getUser().getFirstName());
        assertEquals(DEFAULT_LAST_NAME, traineeWithCredentials.getUser().getLastName());
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingNullTrainee() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTrainee(null));

        assertEquals("Trainee cannot be null", exception.getMessage());
        verifyNoInteractions(repository, generator, passwordEncoder);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCreatingTraineeWithoutUser() {
        Trainee invalidTrainee = buildTraineeWithoutUser();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTrainee(invalidTrainee));

        assertEquals("User cannot be null", exception.getMessage());
        verifyNoInteractions(repository, generator, passwordEncoder);
    }

    @Test
    void shouldGetTraineeByUsernameSuccessfully() {
        Trainee expected = buildTraineeWithId();
        when(repository.findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME)).thenReturn(Optional.of(expected));

        Trainee actual = service.getTraineeByUsername(DEFAULT_USERNAME);

        assertEquals(expected, actual);
        verify(repository).findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME);
    }

    @Test
    void shouldThrowEntityNotFoundWhenTraineeUsernameNotFound() {
        String nonExistentUsername = "unknown";
        when(repository.findByUsernameWithUserAndTrainersDetails(nonExistentUsername)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.getTraineeByUsername(nonExistentUsername));

        assertEquals(buildNotFoundMessage(nonExistentUsername), exception.getMessage());
        verify(repository).findByUsernameWithUserAndTrainersDetails(nonExistentUsername);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenGettingTraineeWithNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.getTraineeByUsername(null));

        assertEquals("Trainee username cannot be null", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldGetAvailableTrainersSuccessfully() {
        List<Trainer> expected = List.of(buildTrainerWithId(1L));

        when(repository.existsByUserUsername(DEFAULT_USERNAME)).thenReturn(true);
        when(trainerRepository.findTraineeAvailableTrainers(DEFAULT_USERNAME)).thenReturn(expected);

        List<Trainer> actual = service.getAvailableTrainers(DEFAULT_USERNAME);

        assertEquals(expected, actual);
        verify(repository).existsByUserUsername(DEFAULT_USERNAME);
        verify(trainerRepository).findTraineeAvailableTrainers(DEFAULT_USERNAME);
    }

    @Test
    void shouldThrowEntityNotFoundWhenGettingAvailableTrainersForUnknownTrainee() {
        when(repository.existsByUserUsername(DEFAULT_USERNAME)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.getAvailableTrainers(DEFAULT_USERNAME));

        assertEquals(buildNotFoundMessage(DEFAULT_USERNAME), exception.getMessage());
        verify(repository).existsByUserUsername(DEFAULT_USERNAME);
        verify(trainerRepository, never()).findTraineeAvailableTrainers(any());
    }

    @Test
    void shouldGetTraineeTrainingsSuccessfully() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder().username(DEFAULT_USERNAME).build();
        List<Training> expected = List.of(Training.builder().id(1L).build());
        @SuppressWarnings("unchecked")
        Specification<Training> spec = mock(Specification.class);

        when(trainingCriteriaBuilder.build(filter)).thenReturn(spec);
        when(trainingRepository.findAll(spec)).thenReturn(expected);

        List<Training> actual = service.getTraineeTrainings(filter);

        verify(trainingCriteriaBuilder).build(filter);
        verify(trainingRepository).findAll(spec);
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenGettingTrainingsByNullCriteria() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.getTraineeTrainings(null));

        assertEquals("Filter cannot be null", exception.getMessage());
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldReturnTrueWhenUsernameAndPasswordMatches() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);

        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches(DEFAULT_PASSWORD, trainee.getUser().getPassword())).thenReturn(true);

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertTrue(result);
        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(passwordEncoder).matches(DEFAULT_PASSWORD, trainee.getUser().getPassword());
    }

    @Test
    void shouldReturnFalseWhenUsernameNotFound() {
        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertFalse(result);
        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);

        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("wrong-password", trainee.getUser().getPassword())).thenReturn(false);

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, "wrong-password");

        assertFalse(result);
        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(passwordEncoder).matches("wrong-password", trainee.getUser().getPassword());
    }

    @Test
    void shouldUpdateTraineeSuccessfully() {
        Trainee existingTrainee = buildTraineeWithId();
        Trainee updateRequest = existingTrainee.toBuilder()
                .address("New Address")
                .build();

        when(repository.findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME)).thenReturn(Optional.of(existingTrainee));
        when(repository.save(any(Trainee.class))).thenReturn(updateRequest);

        Trainee actual = service.updateTrainee(updateRequest);

        verify(repository).findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME);
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(repository).save(traineeCaptor.capture());
        assertEquals("New Address", traineeCaptor.getValue().getAddress());
        assertEquals(updateRequest, actual);
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingUnknownTrainee() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);

        when(repository.findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateTrainee(trainee));

        assertEquals(buildNotFoundMessage(DEFAULT_USERNAME), exception.getMessage());
        verify(repository, never()).save(any(Trainee.class));
    }

    @Test
    void shouldUpdateTraineeTrainersSuccessfully() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);
        Trainer trainer1 = buildTrainerWithId(2L);
        List<String> trainerUsernames = List.of("billy.harrington");

        when(repository.findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findTraineeTrainersByUsernames(trainerUsernames)).thenReturn(List.of(trainer1));
        when(repository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee actual = service.updateTraineeTrainers(DEFAULT_USERNAME, trainerUsernames);

        verify(repository).findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME);
        verify(trainerRepository).findTraineeTrainersByUsernames(trainerUsernames);
        verify(repository).save(any(Trainee.class));
        assertNotNull(actual);
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingTrainersForUnknownTrainee() {
        List<String> trainerUsernames = List.of("billy.harrington");
        when(repository.findByUsernameWithUserAndTrainersDetails(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateTraineeTrainers(DEFAULT_USERNAME, trainerUsernames));

        assertEquals(buildNotFoundMessage(DEFAULT_USERNAME), exception.getMessage());
        verify(trainerRepository, never()).findTraineeTrainersByUsernames(any());
        verify(repository, never()).save(any(Trainee.class));
    }

    @Test
    void shouldUpdateActivationStatusToActiveSuccessfully() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);
        User inactiveUser = trainee.getUser().toBuilder().isActive(false).build();
        Trainee inactiveTrainee = trainee.toBuilder().user(inactiveUser).build();

        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        service.updateActivationStatus(DEFAULT_USERNAME, true);

        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().getUser().getIsActive());
    }

    @Test
    void shouldThrowConflictExceptionWhenActivatingAlreadyActiveTrainee() {
        Trainee activeTrainee = buildTraineeWithUsername(DEFAULT_USERNAME);

        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(activeTrainee));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, true));

        assertEquals("Trainee with username: " + DEFAULT_USERNAME + " is already active", exception.getMessage());
        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(repository, never()).save(any(Trainee.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenDeactivatingAlreadyInactiveTrainee() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);
        User inactiveUser = trainee.getUser().toBuilder().isActive(false).build();
        Trainee inactiveTrainee = trainee.toBuilder().user(inactiveUser).build();

        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, false));

        assertEquals("Trainee with username: " + DEFAULT_USERNAME + " is already inactive", exception.getMessage());
        verify(repository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(repository, never()).save(any(Trainee.class));
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingActivationStatusForUnknownTrainee() {
        when(repository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, true));

        assertEquals(buildNotFoundMessage(DEFAULT_USERNAME), exception.getMessage());
        verify(repository, never()).save(any(Trainee.class));
    }

    @Test
    void shouldDeleteTraineeByUsernameSuccessfully() {
        Training training = Training.builder()
                .trainingDate(LocalDate.of(2025, MAY, 24))
                .trainingDuration(60)
                .build();
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME).toBuilder()
                .trainings(List.of(training))
                .build();

        when(repository.findByUsername(DEFAULT_USERNAME)).thenReturn(Optional.of(trainee));

        boolean result = service.deleteTraineeByUsername(DEFAULT_USERNAME);

        assertTrue(result);
        verify(repository).delete(trainee);
        verify(workloadClientFacade).deleteWorkload(training);
    }

    @Test
    void shouldReturnFalseWhenDeletingByNonExistentUsername() {
        String nonExistingUsername = "unknown";

        when(repository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

        boolean result = service.deleteTraineeByUsername(nonExistingUsername);

        assertFalse(result);
        verify(repository, never()).delete(any());
    }

    private String buildNotFoundMessage(String username) {
        return "Trainee not found with username: " + username;
    }

}
