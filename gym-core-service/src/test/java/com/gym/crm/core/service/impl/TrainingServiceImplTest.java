package com.gym.crm.core.service.impl;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.APRIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final String TRAINEE_USERNAME = "liam.miller";
    private static final String TRAINER_USERNAME = "billy.harrington";
    private static final String TRAINING_TYPE_NAME = "STRENGTH";
    private static final Long DEFAULT_TRAINING_TYPE_ID = 1L;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void shouldCreateTrainingWhenParticipantsExist() {
        Training training = buildTrainingWithUsernames();
        TrainingType trainingType = TrainingType.builder().id(DEFAULT_TRAINING_TYPE_ID).trainingTypeName(TRAINING_TYPE_NAME).build();
        Trainee existingTrainee = Trainee.builder().id(1L).build();
        Trainer existingTrainer = Trainer.builder().id(2L).specialization(trainingType).build();
        Training expected = Training.builder()
                .id(1L)
                .trainee(existingTrainee)
                .trainer(existingTrainer)
                .trainingType(trainingType)
                .build();

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(expected);

        Training actual = service.createTraining(training);

        assertEquals(expected, actual);
        verify(traineeRepository).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void shouldThrowNullPointerWhenCreatingNullTraining() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTraining(null));

        assertEquals("Training cannot be null", exception.getMessage());
        verifyNoInteractions(trainingRepository, traineeRepository, trainerRepository, trainingTypeRepository);
    }

    @Test
    void shouldThrowNullPointerWhenTraineeIsNull() {
        Training training = Training.builder()
                .trainer(buildTrainerWithUsername())
                .trainingName("Test")
                .trainingDate(LocalDate.of(2026, APRIL, 15))
                .trainingDuration(60)
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTraining(training));

        assertEquals("Trainee cannot be null", exception.getMessage());
        verifyNoInteractions(trainingRepository, traineeRepository, trainerRepository, trainingTypeRepository);
    }

    @Test
    void shouldThrowNullPointerWhenTrainerIsNull() {
        Training training = Training.builder()
                .trainee(buildTraineeWithUsername())
                .trainingName("Test")
                .trainingDate(LocalDate.of(2026, APRIL, 15))
                .trainingDuration(60)
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.createTraining(training));

        assertEquals("Trainer cannot be null", exception.getMessage());
        verifyNoInteractions(trainingRepository, traineeRepository, trainerRepository, trainingTypeRepository);
    }

    @Test
    void shouldThrowEntityNotFoundWhenTraineeDoesNotExist() {
        Training training = buildTrainingWithUsernames();

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.createTraining(training));

        assertEquals("Trainee not found with username: " + TRAINEE_USERNAME, exception.getMessage());
        verify(traineeRepository).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository, never()).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldThrowEntityNotFoundWhenTrainerDoesNotExist() {
        Training training = buildTrainingWithUsernames();
        Trainee existingTrainee = Trainee.builder().id(1L).build();

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(existingTrainee));
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.createTraining(training));

        assertEquals("Trainer not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(traineeRepository).findByUsername(TRAINEE_USERNAME);
        verify(trainerRepository).findByUsername(TRAINER_USERNAME);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllTrainingTypes() {
        TrainingType type1 = TrainingType.builder().id(1L).trainingTypeName("Cardio").build();
        TrainingType type2 = TrainingType.builder().id(2L).trainingTypeName("Yoga").build();
        List<TrainingType> expected = List.of(type1, type2);

        when(trainingTypeRepository.findAll()).thenReturn(expected);

        List<TrainingType> actual = service.getAllTrainingTypes();

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(trainingTypeRepository).findAll();
    }

    @Test
    void shouldReturnEmptyTrainingTypes() {
        List<TrainingType> expected = List.of();

        when(trainingTypeRepository.findAll()).thenReturn(expected);

        List<TrainingType> actual = service.getAllTrainingTypes();

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(trainingTypeRepository).findAll();
    }

    private Training buildTrainingWithUsernames() {
        return Training.builder()
                .trainee(buildTraineeWithUsername())
                .trainer(buildTrainerWithUsername())
                .trainingName("Morning Workout")
                .trainingDate(LocalDate.of(2026, APRIL, 15))
                .trainingDuration(60)
                .build();
    }

    private Trainee buildTraineeWithUsername() {
        User user = User.builder().username(TRAINEE_USERNAME).build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Trainer buildTrainerWithUsername() {
        User user = User.builder().username(TRAINER_USERNAME).build();
        TrainingType specialization = TrainingType.builder().id(DEFAULT_TRAINING_TYPE_ID).trainingTypeName(TRAINING_TYPE_NAME).build();

        return Trainer.builder()
                .id(2L)
                .user(user)
                .specialization(specialization)
                .build();
    }

}
