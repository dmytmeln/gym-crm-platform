package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.repository.specification.TrainerTrainingCriteriaBuilder;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.common.ProfileCredentialGenerator;
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

@SpringJUnitConfig(TrainerServiceImpl.class)
@EnableMethodSecurity
class TrainerServiceSecurityTest {

    @MockitoBean
    private TrainerRepository trainerRepository;

    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;

    @MockitoBean
    private TrainingRepository trainingRepository;

    @MockitoBean
    private ProfileCredentialGenerator credentialGenerator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private TrainerTrainingCriteriaBuilder trainingCriteriaBuilder;

    @Autowired
    private TrainerService service;

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowGetTrainerByUsernameWhenIsSelf() {
        Trainer trainer = new Trainer();

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails("marcus.stone")).thenReturn(Optional.of(trainer));

        Trainer result = service.getTrainerByUsername("marcus.stone");

        assertThat(result).isEqualTo(trainer);
    }

    @Test
    @WithMockUser(username = "other.trainer", roles = "TRAINER")
    void shouldDenyGetTrainerByUsernameWhenIsOtherTrainer() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTrainerByUsername("marcus.stone"));
    }

    @Test
    @WithMockUser(username = "liam.miller", roles = "TRAINEE")
    void shouldDenyGetTrainerByUsernameWhenIsTrainee() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTrainerByUsername("marcus.stone"));
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowGetTrainerTrainingsWhenIsSelf() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username("marcus.stone")
                .build();

        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> result = service.getTrainerTrainings(filter);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "other.trainer", roles = "TRAINER")
    void shouldDenyGetTrainerTrainingsWhenIsOtherTrainer() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username("marcus.stone")
                .build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.getTrainerTrainings(filter));
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowUpdateTrainerWhenIsSelf() {
        User user = User.builder().username("marcus.stone").build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUsernameWithUserAndTraineesDetails("marcus.stone")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = service.updateTrainer(trainer);

        assertThat(result).isEqualTo(trainer);
    }

    @Test
    @WithMockUser(username = "other.trainer", roles = "TRAINER")
    void shouldDenyUpdateTrainerWhenIsOtherTrainer() {
        User user = User.builder().username("marcus.stone").build();
        Trainer trainer = Trainer.builder().user(user).build();

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.updateTrainer(trainer));
    }

    @Test
    @WithMockUser(username = "marcus.stone", roles = "TRAINER")
    void shouldAllowUpdateActivationStatusWhenIsSelf() {
        User user = User.builder().username("marcus.stone").isActive(false).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUsernameWithUser("marcus.stone")).thenReturn(Optional.of(trainer));

        assertThatNoException().isThrownBy(() -> service.updateActivationStatus("marcus.stone", true));
    }

    @Test
    @WithMockUser(username = "other.trainer", roles = "TRAINER")
    void shouldDenyUpdateActivationStatusWhenIsOtherTrainer() {
        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.updateActivationStatus("marcus.stone", true));
    }

}
