package com.gym.crm.core.repository;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTraineeConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForDirectFields;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForSaved;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingTypeConfigForDirectFields;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerRepositoryTest extends AbstractRepositoryTest<TrainerRepository> {

    private static final long EXISTING_ID = 1L;
    private static final String EXISTING_USERNAME = "marcus.stone";
    private static final String NON_EXISTING_USERNAME = "non.existent";
    private static final String EXISTING_TRAINEE_USERNAME = "liam.miller";
    private static final int TRAINERS_COUNT = 3;
    private static final int USERS_COUNT = 6;
    private static final int TRAINEES_COUNT = 2;
    private static final int TRAININGS_COUNT = 2;
    private static final int TRAINING_TYPES_COUNT = 3;
    private static final long SPECIALIZATION_ID = 1L;

    @Test
    void shouldSaveTrainerAndUser() {
        User validUser = User.builder()
                .firstName("Robert")
                .lastName("Downey")
                .username("robert.downey")
                .password("ironman123")
                .isActive(true)
                .build();
        TrainingType specialization = TrainingType.builder()
                .id(SPECIALIZATION_ID)
                .trainingTypeName("CARDIO")
                .build();
        Trainer validTrainer = Trainer.builder()
                .user(validUser)
                .specialization(specialization)
                .build();

        Trainer actual = repository.save(validTrainer);

        assertThat(testDbClient.countTrainers())
                .as("Only 1 trainer should be inserted")
                .isEqualTo(TRAINERS_COUNT + 1);
        assertThat(testDbClient.countUsers())
                .as("Only 1 user should be inserted")
                .isEqualTo(USERS_COUNT + 1);
        assertThat(testDbClient.countTrainees())
                .as("Trainees should be unchanged")
                .isEqualTo(TRAINEES_COUNT);
        assertThat(testDbClient.countTrainings())
                .as("Trainings should be unchanged")
                .isEqualTo(TRAININGS_COUNT);
        assertThat(testDbClient.countTrainingTypes())
                .as("Training types should be unchanged")
                .isEqualTo(TRAINING_TYPES_COUNT);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getId()).isNotNull();
        assertThat(actual.getTrainings()).isEmpty();
        assertThat(actual.getTrainees()).isEmpty();
        assertThat(actual)
                .usingRecursiveComparison(getTrainerConfigForSaved())
                .isEqualTo(validTrainer);
        assertThat(actual.getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(validTrainer.getSpecialization());
        Trainer existingTrainer = testDbClient.findTrainer(actual.getId());
        assertThat(existingTrainer.getId()).isEqualTo(actual.getId());
        assertThat(existingTrainer.getUser().getId()).isEqualTo(actual.getUser().getId());
        assertThat(existingTrainer)
                .usingRecursiveComparison(getTrainerConfigForSaved())
                .isEqualTo(actual);
        assertThat(existingTrainer.getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(actual.getSpecialization());
        assertThat(testDbClient.countTrainerTrainings(actual.getId()))
                .as("New trainer should have no trainings in database")
                .isZero();
        assertThat(testDbClient.countTrainerTrainees(actual.getId()))
                .as("New trainer should have no trainees linked in database")
                .isZero();
    }

    @Test
    void shouldFindByUsernameWhenExists() {
        Trainer expected = testDbClient.findTrainer(EXISTING_ID);

        Optional<Trainer> actual = repository.findByUsername(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTrainerConfigForDirectFields())
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsername() {
        Optional<Trainer> actual = repository.findByUsername(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindAllTrainers() {
        List<Trainer> expectedTrainers = List.of(testDbClient.findTrainer(1L),
                testDbClient.findTrainer(2L), testDbClient.findTrainer(3L));

        List<Trainer> actual = repository.findAll();

        assertThat(actual).hasSize(TRAINERS_COUNT);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainerConfigForExisting())
                .containsExactlyElementsOf(expectedTrainers);
        assertThat(actual.get(0).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.get(0).getSpecialization());
        assertThat(actual.get(1).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.get(1).getSpecialization());
        assertThat(actual.get(2).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.get(2).getSpecialization());
    }

    @Test
    void shouldFindTraineeAvailableTrainers() {
        List<Trainer> expectedAvailableTrainers = List.of(testDbClient.findTrainer(2L),
                testDbClient.findTrainer(3L));

        List<Trainer> actual = repository.findTraineeAvailableTrainers(EXISTING_TRAINEE_USERNAME);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainerConfigForExisting())
                .containsExactlyElementsOf(expectedAvailableTrainers);
        assertThat(actual.get(0).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedAvailableTrainers.get(0).getSpecialization());
        assertThat(actual.get(1).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedAvailableTrainers.get(1).getSpecialization());
    }

    @Test
    void shouldFindTraineeTrainersByUsernames() {
        List<Trainer> expectedTrainers = List.of(testDbClient.findTrainer(1L),
                testDbClient.findTrainer(2L));
        List<String> usernames = List.of("marcus.stone", "sarah.adams");

        List<Trainer> actual = repository.findTraineeTrainersByUsernames(usernames);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainerConfigForExisting())
                .containsExactlyElementsOf(expectedTrainers);
        assertThat(actual.get(0).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.get(0).getSpecialization());
        assertThat(actual.get(1).getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainers.get(1).getSpecialization());
    }

    @Test
    void shouldFindByUsernameWithUserAndTraineesDetailsWhenExists() {
        Trainer expected = testDbClient.findTrainer(EXISTING_ID);
        List<Trainee> expectedTrainees = List.of(testDbClient.findTrainee(1L));

        Optional<Trainer> actual = repository.findByUsernameWithUserAndTraineesDetails(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTrainerConfigForExisting())
                .isEqualTo(expected);
        assertThat(actual.get().getSpecialization())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expected.getSpecialization());
        assertThat(actual.get().getTrainees())
                .hasSize(1)
                .usingRecursiveComparison(getTraineeConfigForExisting())
                .isEqualTo(expectedTrainees);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsernameWithUserAndTraineesDetails() {
        Optional<Trainer> actual = repository.findByUsernameWithUserAndTraineesDetails(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldFindByUsernameWithUserWhenExists() {
        Trainer expected = testDbClient.findTrainer(EXISTING_ID);

        Optional<Trainer> actual = repository.findByUsernameWithUser(EXISTING_USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(getTrainerConfigForExisting())
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByUsernameWithUser() {
        Optional<Trainer> actual = repository.findByUsernameWithUser(NON_EXISTING_USERNAME);

        assertThat(actual).isEmpty();
    }

}
