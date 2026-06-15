package com.gym.crm.core.repository;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTraineeConfigForDirectFields;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTraineeConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTraineeConfigForSaved;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForDirectFields;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingTypeConfigForDirectFields;
import static java.time.Month.APRIL;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest extends AbstractRepositoryTest<TraineeRepository> {

    private static final long EXISTING_ID = 1L;
    private static final String EXISTING_USERNAME = "liam.miller";
    private static final String NON_EXISTING_USERNAME = "non.existent";
    private static final int TRAINEES_COUNT = 2;
    private static final int USERS_COUNT = 6;
    private static final int TRAINERS_COUNT = 3;
    private static final int TRAININGS_COUNT = 2;
    private static final int TRAINING_TYPES_COUNT = 3;

    @Test
    void shouldSaveTraineeAndUser() {
        User validUser = User.builder()
                .firstName("Robert")
                .lastName("Downey")
                .username("robert.downey")
                .password("ironman123")
                .isActive(true)
                .build();
        Trainee validTrainee = Trainee.builder()
                .user(validUser)
                .address("Malibu, CA")
                .dateOfBirth(LocalDate.of(1965, APRIL, 4))
                .build();

        Trainee actual = repository.save(validTrainee);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison(getTraineeConfigForSaved())
                .isEqualTo(validTrainee);
        Trainee existingTrainee = testDbClient.findTrainee(actual.getId());
        assertThat(existingTrainee)
                .usingRecursiveComparison(getTraineeConfigForSaved())
                .isEqualTo(actual);
        assertThat(testDbClient.countTraineeTrainings(actual.getId())).as("New trainee should have no trainings in database").isZero();
        assertThat(testDbClient.countTraineeTrainers(actual.getId())).as("New trainee should have no trainers in database").isZero();
    }

    @Test
    void shouldFindByUsernameWithUserAndTrainersDetailsWhenExists() {
        Trainee expected = testDbClient.findTrainee(EXISTING_ID);
        List<Trainer> expectedTrainers = List.of(testDbClient.findTrainer(1L));

        Optional<Trainee> actual = repository.findByUsernameWithUserAndTrainersDetails(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTraineeConfigForExisting())
                .isEqualTo(expected);
        assertThat(actual.get().getTrainers())
                .hasSize(1)
                .usingRecursiveComparison(getTrainerConfigForExisting())
                .isEqualTo(expectedTrainers);
        assertThat(actual.get().getTrainers().iterator().next().getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.getFirst().getSpecialization());
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsername() {
        Optional<Trainee> actual = repository.findByUsernameWithUserAndTrainersDetails(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllTrainees() {
        List<Trainee> expectedTrainees = List.of(testDbClient.findTrainee(EXISTING_ID), testDbClient.findTrainee(2L));
        List<Trainer> firstTraineeExpectedTrainers = List.of(testDbClient.findTrainer(1L));

        List<Trainee> actual = repository.findAll();

        assertThat(actual).hasSize(TRAINEES_COUNT);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTraineeConfigForExisting())
                .containsExactlyElementsOf(expectedTrainees);
        assertThat(actual.get(0).getTrainers())
                .hasSize(1)
                .usingRecursiveComparison(getTrainerConfigForExisting())
                .isEqualTo(firstTraineeExpectedTrainers);
        assertThat(actual.get(0).getTrainers().iterator().next().getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(firstTraineeExpectedTrainers.getFirst().getSpecialization());
        assertThat(actual.get(1).getTrainers()).isEmpty();
    }

    @Test
    void shouldUpdateTrainee() {
        List<Trainer> expectedTrainers = testDbClient.findTraineeTrainers(EXISTING_ID);
        List<Training> expectedTrainings = List.of(testDbClient.findTrainingSimple(1L));
        Trainee existingTrainee = testDbClient.findTrainee(EXISTING_ID);
        User updatedUser = existingTrainee.getUser().toBuilder()
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .isActive(false)
                .build();
        Trainee updatedTrainee = existingTrainee.toBuilder()
                .address("Updated Address")
                .dateOfBirth(LocalDate.of(1995, JANUARY, 1))
                .user(updatedUser)
                .trainers(new HashSet<>(expectedTrainers))
                .build();

        Trainee actual = repository.save(updatedTrainee);

        assertThat(testDbClient.countTrainees()).as("Trainees count should be unchanged").isEqualTo(TRAINEES_COUNT);
        assertThat(testDbClient.countUsers()).as("Users count should be unchanged").isEqualTo(USERS_COUNT);
        assertThat(testDbClient.countTrainers()).as("Trainers count should be unchanged").isEqualTo(TRAINERS_COUNT);
        assertThat(testDbClient.countTrainings()).as("Trainings count should be unchanged").isEqualTo(TRAININGS_COUNT);
        assertThat(testDbClient.countTrainingTypes()).as("Training types count should be unchanged").isEqualTo(TRAINING_TYPES_COUNT);
        assertThat(actual)
                .usingRecursiveComparison(getTraineeConfigForExisting())
                .isEqualTo(updatedTrainee);
        assertThat(actual.getTrainers())
                .hasSize(1)
                .usingRecursiveComparison(getTrainerConfigForDirectFields())
                .isEqualTo(updatedTrainee.getTrainers());
        Trainee existingTraineeAfterUpdate = testDbClient.findTrainee(EXISTING_ID);
        assertThat(existingTraineeAfterUpdate.getId()).isEqualTo(actual.getId());
        assertThat(existingTraineeAfterUpdate.getUser().getId()).isEqualTo(actual.getUser().getId());
        assertThat(existingTraineeAfterUpdate)
                .usingRecursiveComparison(getTraineeConfigForExisting())
                .isEqualTo(actual);
        assertThat(testDbClient.findTraineeTrainers(EXISTING_ID))
                .hasSize(1)
                .usingRecursiveComparison(getTrainerConfigForDirectFields())
                .isEqualTo(expectedTrainers);
        assertThat(testDbClient.findTraineeTrainings(EXISTING_ID))
                .hasSize(1)
                .usingRecursiveComparison(getTrainingConfigForExisting())
                .isEqualTo(expectedTrainings);
    }

    @Test
    void shouldDeleteByUsernameWhenExists() {
        Trainee trainee = repository.findByUsernameWithUserAndTrainersDetails(EXISTING_USERNAME).orElseThrow();
        Long existingTraineeTrainerId = 1L;

        repository.delete(trainee);

        assertThat(testDbClient.traineeExists(EXISTING_ID)).as("Trainee should be deleted").isFalse();
        assertThat(testDbClient.userExists(EXISTING_USERNAME)).as("User should be deleted").isFalse();
        assertThat(testDbClient.countTrainees()).as("Trainees count should decrease by 1").isEqualTo(TRAINEES_COUNT - 1);
        assertThat(testDbClient.countUsers()).as("Users count should decrease by 1").isEqualTo(USERS_COUNT - 1);
        assertThat(testDbClient.countTrainers()).as("Trainers count should be unchanged").isEqualTo(TRAINERS_COUNT);
        assertThat(testDbClient.countTrainings()).as("Trainings count should decrease by 1").isEqualTo(TRAININGS_COUNT - 1);
        assertThat(testDbClient.countTrainingTypes()).as("Training types count should be unchanged").isEqualTo(TRAINING_TYPES_COUNT);
        assertThat(testDbClient.countTraineeTrainings(EXISTING_ID)).as("Trainee's trainings should be deleted").isZero();
        assertThat(testDbClient.countTraineeTrainers(EXISTING_ID)).as("Trainee's trainers links should be deleted").isZero();
        assertThat(testDbClient.trainerExists(existingTraineeTrainerId)).as("Trainee's trainer should remain").isTrue();
    }

    @Test
    void shouldFindByUsernameWithUserWhenExists() {
        Trainee expected = testDbClient.findTrainee(EXISTING_ID);

        Optional<Trainee> actual = repository.findByUsernameWithUser(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTraineeConfigForExisting())
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsernameWithUser() {
        Optional<Trainee> actual = repository.findByUsernameWithUser(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindByUsernameDirectWhenExists() {
        Trainee expected = testDbClient.findTrainee(EXISTING_ID);

        Optional<Trainee> actual = repository.findByUsername(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTraineeConfigForDirectFields())
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsernameDirect() {
        Optional<Trainee> actual = repository.findByUsername(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldExistByUserUsernameWhenExists() {
        boolean actual = repository.existsByUserUsername(EXISTING_USERNAME);

        assertThat(actual).isTrue();
    }

    @Test
    void shouldNotExistByUserUsernameWhenNotExists() {
        boolean actual = repository.existsByUserUsername(NON_EXISTING_USERNAME);

        assertThat(actual).isFalse();
    }

}

