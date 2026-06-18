package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.specification.TraineeTrainingCriteriaBuilder;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.common.ProfileCredentialGenerator;
import com.gym.crm.core.client.WorkloadClientFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(TraineeServiceImpl.class)
@EnableMethodSecurity
class TraineeServiceSecurityTest {

    @MockitoBean
    private TraineeRepository traineeRepository;

    @MockitoBean
    private TrainerRepository trainerRepository;

    @MockitoBean
    private TrainingRepository trainingRepository;

    @MockitoBean
    private ProfileCredentialGenerator credentialGenerator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private TraineeTrainingCriteriaBuilder trainingCriteriaBuilder;

    @MockitoBean
    private WorkloadClientFacade workloadClientFacade;

    @Autowired
    private TraineeService service;

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowGetTraineeByUsernameWhenIsSelf() {
        Trainee trainee = new Trainee();

        when(traineeRepository.findByUsernameWithUserAndTrainersDetails("liam.miller")).thenReturn(Optional.of(trainee));

        Trainee result = service.getTraineeByUsername("liam.miller");

        assertThat(result).isEqualTo(trainee);
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyGetTraineeByUsernameWhenIsOtherTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTraineeByUsername("liam.miller"));
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldDenyGetTraineeByUsernameWhenIsTrainer() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTraineeByUsername("liam.miller"));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowGetAvailableTrainersWhenIsSelf() {
        when(traineeRepository.existsByUserUsername("liam.miller")).thenReturn(true);
        when(trainerRepository.findTraineeAvailableTrainers("liam.miller")).thenReturn(List.of());

        List<Trainer> result = service.getAvailableTrainers("liam.miller");

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyGetAvailableTrainersWhenIsOtherTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getAvailableTrainers("liam.miller"));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowGetTraineeTrainingsWhenIsSelf() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username("liam.miller")
                .build();

        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> result = service.getTraineeTrainings(filter);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyGetTraineeTrainingsWhenIsOtherTrainee() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username("liam.miller")
                .build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTraineeTrainings(filter));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowUpdateTraineeWhenIsSelf() {
        User user = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUsernameWithUserAndTrainersDetails("liam.miller")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = service.updateTrainee(trainee);

        assertThat(result).isEqualTo(trainee);
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyUpdateTraineeWhenIsOtherTrainee() {
        User user = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(user).build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.updateTrainee(trainee));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowUpdateTraineeTrainersWhenIsSelf() {
        User user = User.builder().username("liam.miller").build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUsernameWithUserAndTrainersDetails("liam.miller")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTraineeTrainersByUsernames(any())).thenReturn(List.of());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = service.updateTraineeTrainers("liam.miller", List.of());

        assertThat(result).isEqualTo(trainee);
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyUpdateTraineeTrainersWhenIsOtherTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.updateTraineeTrainers("liam.miller", List.of()));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowUpdateActivationStatusWhenIsSelf() {
        User user = User.builder().username("liam.miller").isActive(false).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUsernameWithUser("liam.miller")).thenReturn(Optional.of(trainee));

        assertThatNoException().isThrownBy(() -> service.updateActivationStatus("liam.miller", true));
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyUpdateActivationStatusWhenIsOtherTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.updateActivationStatus("liam.miller", true));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldAllowDeleteTraineeWhenIsSelf() {
        Trainee trainee = new Trainee();

        when(traineeRepository.findByUsername("liam.miller")).thenReturn(Optional.of(trainee));

        boolean result = service.deleteTraineeByUsername("liam.miller");

        assertThat(result).isTrue();
    }

    @Test
    @WithMockUser(username = "other.trainee", roles = "TRAINEE")
    void shouldDenyDeleteTraineeWhenIsOtherTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.deleteTraineeByUsername("liam.miller"));
    }

}
