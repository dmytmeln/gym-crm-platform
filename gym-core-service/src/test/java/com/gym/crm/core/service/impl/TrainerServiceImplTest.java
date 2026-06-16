package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.ConflictException;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.repository.specification.TrainerTrainingCriteriaBuilder;
import com.gym.crm.core.service.common.ProfileCredentialGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_FIRST_NAME;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_LAST_NAME;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_PASSWORD;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_SPECIALIZATION;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_TRAINER_ID;
import static com.gym.crm.core.factory.TrainerTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithId;
import static com.gym.crm.core.factory.TrainerTestFactory.buildTrainerWithoutCredentials;
import static com.gym.crm.core.factory.TrainerTestFactory.getDefaultTrainerBuilder;
import static com.gym.crm.core.factory.TrainerTestFactory.getDefaultTrainingTypeBuilder;
import static com.gym.crm.core.factory.TrainerTestFactory.getDefaultUserBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private static final String NULL_MSG = "Trainer cannot be null";
    private static final String USERNAME_NULL_MSG = "Trainer username cannot be null";
    private static final String USER_NULL_MSG = "Trainer user cannot be null";

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private ProfileCredentialGenerator generator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TrainerTrainingCriteriaBuilder trainingCriteriaBuilder;

    @InjectMocks
    private TrainerServiceImpl service;

    @Test
    void shouldCreateTrainerWithGeneratedCredentials() {
        Trainer trainerWithoutCredentials = buildTrainerWithoutCredentials();
        Trainer expected = buildTrainerWithId(DEFAULT_TRAINER_ID);
        String encodedPassword = "encoded-" + DEFAULT_PASSWORD;

        when(trainingTypeRepository.findByTrainingTypeName(DEFAULT_SPECIALIZATION)).thenReturn(Optional.of(trainerWithoutCredentials.getSpecialization()));
        when(generator.generateUsername(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME)).thenReturn(DEFAULT_USERNAME);
        when(generator.generatePassword()).thenReturn(DEFAULT_PASSWORD);
        when(passwordEncoder.encode(DEFAULT_PASSWORD)).thenReturn(encodedPassword);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(expected);

        Trainer actual = service.createTrainer(trainerWithoutCredentials);

        verify(generator).generateUsername(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME);
        verify(generator).generatePassword();
        verify(passwordEncoder).encode(DEFAULT_PASSWORD);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer trainerWithCredentials = trainerCaptor.getValue();
        assertEquals(DEFAULT_USERNAME, trainerWithCredentials.getUser().getUsername());
        assertEquals(encodedPassword, trainerWithCredentials.getUser().getPassword());
        assertEquals(DEFAULT_FIRST_NAME, trainerWithCredentials.getUser().getFirstName());
        assertEquals(DEFAULT_LAST_NAME, trainerWithCredentials.getUser().getLastName());
        assertEquals(expected.getSpecialization(), trainerWithCredentials.getSpecialization());
        assertEquals(expected, actual);
        assertEquals(DEFAULT_PASSWORD, actual.getUser().getPassword());
    }

    @Test
    void shouldThrowNullPointerWhenCreatingNullTrainer() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTrainer(null));

        assertEquals(NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainerRepository, generator);
    }

    @Test
    void shouldThrowExceptionWhenCreatingTrainerWithInvalidSpecialization() {
        Trainer trainer = buildTrainerWithoutCredentials();

        when(trainingTypeRepository.findByTrainingTypeName(DEFAULT_SPECIALIZATION)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.createTrainer(trainer));

        assertEquals("TrainingType not found with name: " + DEFAULT_SPECIALIZATION, exception.getMessage());
        verify(trainingTypeRepository).findByTrainingTypeName(DEFAULT_SPECIALIZATION);
        verifyNoInteractions(generator, trainerRepository);
    }

    @Test
    void shouldThrowNullPointerWhenCreatingTrainerWithNullUser() {
        Trainer trainer = Trainer.builder().build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTrainer(trainer));

        assertEquals(USER_NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainingTypeRepository, generator, trainerRepository);
    }

    @Test
    void shouldReturnTrainerByUsername() {
        Trainer expected = buildTrainerWithId();

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME)).thenReturn(Optional.of(expected));

        Trainer actual = service.getTrainerByUsername(DEFAULT_USERNAME);

        assertEquals(expected, actual);
        verify(trainerRepository).findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME);
    }

    @Test
    void shouldThrowExceptionWhenGettingUnknownTrainerByUsername() {
        when(trainerRepository.findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.getTrainerByUsername(DEFAULT_USERNAME));

        assertEquals(buildNotFoundMsg(), exception.getMessage());
    }

    @Test
    void shouldReturnTrainerTrainingsSuccessfully() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder().username(DEFAULT_USERNAME).build();
        List<Training> expected = List.of(Training.builder().id(1L).build());
        @SuppressWarnings("unchecked")
        Specification<Training> spec = mock(Specification.class);

        when(trainingCriteriaBuilder.build(filter)).thenReturn(spec);
        when(trainingRepository.findAll(spec)).thenReturn(expected);

        List<Training> actual = service.getTrainerTrainings(filter);

        verify(trainingCriteriaBuilder).build(filter);
        verify(trainingRepository).findAll(spec);
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnTrueWhenUsernameAndPasswordMatch() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User userWithPassword = trainer.getUser().toBuilder().password("encodedPassword").build();
        trainer = trainer.toBuilder().user(userWithPassword).build();

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(DEFAULT_PASSWORD, "encodedPassword")).thenReturn(true);

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertTrue(result);
        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(passwordEncoder).matches(DEFAULT_PASSWORD, "encodedPassword");
    }

    @Test
    void shouldReturnFalseWhenPasswordDoesNotMatch() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, "wrong");

        assertFalse(result);
        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
    }

    @Test
    void shouldThrowNullPointerWhenMatchingWithNullPassword() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, null));

        assertEquals("Password cannot be null", exception.getMessage());
    }

    @Test
    void shouldReturnFalseWhenNoTrainerExists() {
        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        boolean result = service.doesUsernameAndPasswordMatch(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertFalse(result);
    }

    @Test
    void shouldThrowNullPointerWhenMatchingWithNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.doesUsernameAndPasswordMatch(null, DEFAULT_PASSWORD));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
    }

    @Test
    void shouldMergeUpdatedFieldsAndPreserveExistingUsernameWhenUpdatingTrainer() {
        Trainer existingTrainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        String firstName = "Elena";
        String lastName = "Rodriguez";
        Trainer expected = getDefaultTrainerBuilder()
                .id(DEFAULT_TRAINER_ID)
                .user(getDefaultTrainerBuilder().build().getUser().toBuilder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .password("newPassword")
                        .isActive(false)
                        .build())
                .specialization(getDefaultTrainingTypeBuilder().id(2L).trainingTypeName("Strength").build())
                .build();

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME))
                .thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(expected);

        Trainer actual = service.updateTrainer(expected);

        verify(trainerRepository).findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer mergedTrainer = trainerCaptor.getValue();
        assertEquals(DEFAULT_TRAINER_ID, mergedTrainer.getId());
        assertEquals(DEFAULT_USERNAME, mergedTrainer.getUser().getUsername());
        assertEquals(firstName, mergedTrainer.getUser().getFirstName());
        assertEquals(lastName, mergedTrainer.getUser().getLastName());
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingUnknownTrainer() {
        Trainer updateRequest = buildTrainerWithId(DEFAULT_TRAINER_ID);

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(updateRequest));

        assertEquals(buildNotFoundMsg(), exception.getMessage());
        verify(trainerRepository).findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingNullTrainer() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.updateTrainer(null));

        assertEquals(NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainerRepository, generator);
    }

    @Test
    void shouldUpdateTrainerWithoutChangingSpecialization() {
        Trainer existingTrainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User updatedUser = existingTrainer.getUser().toBuilder().firstName("NewName").build();
        Trainer updateRequest = existingTrainer.toBuilder().user(updatedUser).build();

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updateRequest);

        service.updateTrainer(updateRequest);

        verify(trainerRepository).findByUsernameWithUserAndTraineesDetails(DEFAULT_USERNAME);
        verify(trainingTypeRepository, never()).findByTrainingTypeName(any());
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingTrainerWithNullUser() {
        Trainer trainer = Trainer.builder().id(DEFAULT_TRAINER_ID).build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.updateTrainer(trainer));

        assertEquals(USER_NULL_MSG, exception.getMessage());
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingTrainerWithNullUsername() {
        Trainer trainer = buildTrainerWithoutCredentials().toBuilder()
                .user(getDefaultUserBuilder().username(null).build())
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.updateTrainer(trainer));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
    }

    @Test
    void shouldUpdateActivationStatusToActive() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User inactiveUser = trainer.getUser().toBuilder().isActive(false).build();
        trainer = trainer.toBuilder().user(inactiveUser).build();

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));

        service.updateActivationStatus(DEFAULT_USERNAME, true);

        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        assertEquals(true, trainerCaptor.getValue().getUser().getIsActive());
    }

    @Test
    void shouldUpdateActivationStatusToInactive() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User activeUser = trainer.getUser().toBuilder().isActive(true).build();
        trainer = trainer.toBuilder().user(activeUser).build();

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));

        service.updateActivationStatus(DEFAULT_USERNAME, false);

        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        assertEquals(false, trainerCaptor.getValue().getUser().getIsActive());
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingToAlreadyActiveStatus() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User activeUser = trainer.getUser().toBuilder().isActive(true).build();
        trainer = trainer.toBuilder().user(activeUser).build();

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, true));

        assertEquals("Trainer with username: " + DEFAULT_USERNAME + " is already active", exception.getMessage());
        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingToAlreadyInactiveStatus() {
        Trainer trainer = buildTrainerWithId(DEFAULT_TRAINER_ID);
        User inactiveUser = trainer.getUser().toBuilder().isActive(false).build();
        trainer = trainer.toBuilder().user(inactiveUser).build();

        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.of(trainer));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, false));

        assertEquals("Trainer with username: " + DEFAULT_USERNAME + " is already inactive", exception.getMessage());
        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void shouldThrowNullPointerWhenUpdatingActivationStatusWithNullUsername() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.updateActivationStatus(null, true));

        assertEquals(USERNAME_NULL_MSG, exception.getMessage());
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingActivationStatusForNonExistingTrainer() {
        when(trainerRepository.findByUsernameWithUser(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateActivationStatus(DEFAULT_USERNAME, true));

        assertEquals(buildNotFoundMsg(), exception.getMessage());
        verify(trainerRepository).findByUsernameWithUser(DEFAULT_USERNAME);
    }

    private static String buildNotFoundMsg() {
        return "Trainer not found with username: " + DEFAULT_USERNAME;
    }

}
