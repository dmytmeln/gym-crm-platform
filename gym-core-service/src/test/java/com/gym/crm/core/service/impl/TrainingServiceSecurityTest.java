package com.gym.crm.core.service.impl;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(TrainingServiceImpl.class)
@EnableMethodSecurity
class TrainingServiceSecurityTest {

    @MockitoBean
    private TrainingRepository trainingRepository;

    @MockitoBean
    private TraineeRepository traineeRepository;

    @MockitoBean
    private TrainerRepository trainerRepository;

    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingService service;

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowCreateTrainingWhenIsTrainerOwner() {
        User traineeUser = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        User trainerUser = User.builder().username("marcus.stone").build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Cardio session")
                .build();

        when(traineeRepository.findByUsername("liam.miller")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("marcus.stone")).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(training);

        Training result = service.createTraining(training);

        assertThat(result).isEqualTo(training);
    }

    @Test
    @WithMockUser(username = "other.trainer", roles = "TRAINER")
    void shouldDenyCreateTrainingWhenIsNotTrainerOwner() {
        User traineeUser = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        User trainerUser = User.builder().username("marcus.stone").build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Cardio session")
                .build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.createTraining(training));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldDenyCreateTrainingWhenIsTrainee() {
        User traineeUser = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        User trainerUser = User.builder().username("marcus.stone").build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Cardio session")
                .build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.createTraining(training));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowGetAllTrainingTypesWhenAuthenticatedAsTrainee() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingType> result = service.getAllTrainingTypes();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowGetAllTrainingTypesWhenAuthenticatedAsTrainer() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingType> result = service.getAllTrainingTypes();

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

}
