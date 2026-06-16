package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.ConflictException;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.repository.specification.TrainerTrainingCriteriaBuilder;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.common.ProfileCredentialGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.gym.crm.core.entity.EntityType.TRAINER;
import static com.gym.crm.core.entity.EntityType.TRAINING_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private static final String USERNAME_NULL_MSG = "Trainer username cannot be null";

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingRepository trainingRepository;
    private final ProfileCredentialGenerator credentialGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TrainerTrainingCriteriaBuilder trainingCriteriaBuilder;

    @Override
    @Transactional
    public Trainer createTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer cannot be null");
        Objects.requireNonNull(trainer.getUser(), "Trainer user cannot be null");
        log.info("Creating trainer: {} {}", trainer.getUser().getFirstName(), trainer.getUser().getLastName());

        String trainingTypeName = trainer.getSpecialization().getTrainingTypeName();
        TrainingType trainerSpecialization = trainingTypeRepository.findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> EntityNotFoundException.forName(TRAINING_TYPE, trainingTypeName));

        String username = credentialGenerator.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String password = credentialGenerator.generatePassword();

        User user = trainer.getUser().toBuilder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        Trainer trainerWithCredentialsAndSpecialization = trainer.toBuilder()
                .user(user)
                .specialization(trainerSpecialization)
                .build();

        Trainer createdTrainer = trainerRepository.save(trainerWithCredentialsAndSpecialization);
        log.info("Trainer created with ID: {} and username: {}",
                createdTrainer.getId(), createdTrainer.getUser().getUsername());

        User userWithRawPassword = createdTrainer.getUser().toBuilder()
                .password(password)
                .build();
        return createdTrainer.toBuilder()
                .user(userWithRawPassword)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('TRAINER') and #username == authentication.name")
    public Trainer getTrainerByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);

        return trainerRepository.findByUsernameWithUserAndTraineesDetails(username)
                .orElseThrow(() -> EntityNotFoundException.forUsername(TRAINER, username));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('TRAINER') and #filter.username == authentication.name")
    public List<Training> getTrainerTrainings(TrainerTrainingSearchFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        log.info("Getting trainings for trainer with username: {}", filter.getUsername());

        return trainingRepository.findAll(trainingCriteriaBuilder.build(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesUsernameAndPasswordMatch(String username, String password) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        Objects.requireNonNull(password, "Password cannot be null");
        log.info("Checking if username and password match for trainer username: {}", username);

        Optional<Trainer> trainerOptional = trainerRepository.findByUsernameWithUser(username);
        if (trainerOptional.isEmpty()) {
            log.warn("Trainer not found with username: {}", username);
            return false;
        }

        Trainer trainer = trainerOptional.get();
        boolean passwordMatches = passwordEncoder.matches(password, trainer.getUser().getPassword());
        if (!passwordMatches) {
            log.warn("Passwords do not match for trainer username: {}", username);
            return false;
        }

        log.info("Passwords match for trainer username: {}", username);
        return true;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAINER') and #trainer.user.username == authentication.name")
    public Trainer updateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer cannot be null");
        Objects.requireNonNull(trainer.getUser(), "Trainer user cannot be null");
        Objects.requireNonNull(trainer.getUser().getUsername(), USERNAME_NULL_MSG);
        log.info("Updating trainer with username: {}", trainer.getUser().getUsername());

        Trainer existingTrainer = trainerRepository.findByUsernameWithUserAndTraineesDetails(trainer.getUser().getUsername())
                .orElseThrow(() -> EntityNotFoundException.forUsername(TRAINER, trainer.getUser().getUsername()));

        User updatedUser = existingTrainer.getUser().toBuilder()
                .firstName(trainer.getUser().getFirstName())
                .lastName(trainer.getUser().getLastName())
                .isActive(trainer.getUser().getIsActive())
                .build();
        Trainer mergedTrainer = existingTrainer.toBuilder()
                .user(updatedUser)
                .build();

        Trainer updatedTrainer = trainerRepository.save(mergedTrainer);
        log.info("Trainer with ID: {} and username: {} updated successfully",
                updatedTrainer.getId(), updatedTrainer.getUser().getUsername());

        return updatedTrainer;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TRAINER') and #username == authentication.name")
    public void updateActivationStatus(String username, boolean isActive) {
        Objects.requireNonNull(username, USERNAME_NULL_MSG);
        log.info("Updating activation status for trainer with username: {} to {}", username, isActive);

        Trainer trainer = trainerRepository.findByUsernameWithUser(username)
                .orElseThrow(() -> EntityNotFoundException.forUsername(TRAINER, username));

        if (Objects.equals(trainer.getUser().getIsActive(), isActive)) {
            throw new ConflictException(String.format("Trainer with username: %s is already %s", username, isActive ? "active" : "inactive"));
        }

        User updatedUser = trainer.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerRepository.save(updatedTrainer);
        log.info("Activation status for trainer with username: {} updated successfully", username);
    }

}
