package com.gym.crm.core.repository;

import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTraineeConfigForDirectFields;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainerConfigForDirectFields;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingConfigForExisting;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingConfigForSaved;
import static com.gym.crm.core.helper.EntityRecursiveComparisonConfigs.getTrainingTypeConfigForDirectFields;
import static org.assertj.core.api.Assertions.assertThat;

class TrainingRepositoryTest extends AbstractRepositoryTest<TrainingRepository> {

    private static final int TRAINERS_COUNT = 3;
    private static final int USERS_COUNT = 6;
    private static final int TRAINEES_COUNT = 2;
    private static final int TRAININGS_COUNT = 2;
    private static final int TRAINING_TYPES_COUNT = 3;

    @Test
    void shouldSaveTraining() {
        Trainee validTrainee = Trainee.builder().id(1L).build();
        Trainer validTrainer = Trainer.builder().id(2L).build();
        TrainingType validTrainingType = TrainingType.builder().id(2L).build();
        Training training = Training.builder()
                .trainingName("Evening Yoga")
                .trainingDate(LocalDate.of(2025, 2, 20))
                .trainingDuration(90)
                .trainee(validTrainee)
                .trainer(validTrainer)
                .trainingType(validTrainingType)
                .build();

        Training actual = repository.save(training);

        assertThat(testDbClient.countTrainings()).as("Only 1 training should be inserted").isEqualTo(TRAININGS_COUNT + 1);
        assertThat(testDbClient.countTrainers()).as("Trainers count should be unchanged").isEqualTo(TRAINERS_COUNT);
        assertThat(testDbClient.countTrainees()).as("Trainees count should be unchanged").isEqualTo(TRAINEES_COUNT);
        assertThat(testDbClient.countUsers()).as("Users count should be unchanged").isEqualTo(USERS_COUNT);
        assertThat(testDbClient.countTrainingTypes()).as("Training types count should be unchanged").isEqualTo(TRAINING_TYPES_COUNT);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison(getTrainingConfigForSaved())
                .isEqualTo(training);
        assertThat(actual.getTrainee().getId()).isEqualTo(training.getTrainee().getId());
        assertThat(actual.getTrainer().getId()).isEqualTo(training.getTrainer().getId());
        assertThat(actual.getTrainingType().getId()).isEqualTo(training.getTrainingType().getId());
        Training existingTraining = testDbClient.findTraining(actual.getId());
        assertThat(existingTraining.getId()).isEqualTo(actual.getId());
        assertThat(existingTraining)
                .usingRecursiveComparison(getTrainingConfigForExisting())
                .isEqualTo(actual);
        assertThat(existingTraining.getTrainee().getId()).isEqualTo(actual.getTrainee().getId());
        assertThat(existingTraining.getTrainer().getId()).isEqualTo(actual.getTrainer().getId());
        assertThat(existingTraining.getTrainingType().getId()).isEqualTo(actual.getTrainingType().getId());
    }

    @Test
    void shouldFindTrainerTrainingsByCriteriaAllFilters() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username("marcus.stone")
                .traineeName("Liam Miller")
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 31))
                .build();
        List<Training> expectedTrainings = List.of(testDbClient.findTraining(1L));

        List<Training> actual = repository.findAll(trainerCriteriaBuilder.build(filter));

        assertThat(actual).hasSize(1);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainingConfigForExisting())
                .containsExactlyElementsOf(expectedTrainings);
        assertThat(actual.getFirst().getTrainee())
                .usingRecursiveComparison(getTraineeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainee());
        assertThat(actual.getFirst().getTrainingType())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainingType());
    }

    @Test
    void shouldFindTrainerTrainingsByCriteriaOnlyUsername() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username("marcus.stone")
                .build();
        List<Training> expectedTrainings = List.of(testDbClient.findTraining(1L));

        List<Training> actual = repository.findAll(trainerCriteriaBuilder.build(filter));

        assertThat(actual).hasSize(1);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainingConfigForExisting())
                .containsExactlyElementsOf(expectedTrainings);
        assertThat(actual.getFirst().getTrainee())
                .usingRecursiveComparison(getTraineeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainee());
        assertThat(actual.getFirst().getTrainingType())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainingType());
    }

    @Test
    void shouldFindTraineeTrainingsByCriteriaAllFilters() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username("liam.miller")
                .trainerName("Marcus Stone")
                .trainingTypeName("CARDIO")
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 31))
                .build();
        List<Training> expectedTrainings = List.of(testDbClient.findTraining(1L));

        List<Training> actual = repository.findAll(traineeCriteriaBuilder.build(filter));

        assertThat(actual).hasSize(1);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainingConfigForExisting())
                .containsExactlyElementsOf(expectedTrainings);
        assertThat(actual.getFirst().getTrainer())
                .usingRecursiveComparison(getTrainerConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainer());
        assertThat(actual.getFirst().getTrainingType())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainingType());
    }

    @Test
    void shouldFindTraineeTrainingsByCriteriaOnlyUsername() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username("liam.miller")
                .build();
        List<Training> expectedTrainings = List.of(testDbClient.findTraining(1L));

        List<Training> actual = repository.findAll(traineeCriteriaBuilder.build(filter));

        assertThat(actual).hasSize(1);
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator(getTrainingConfigForExisting())
                .containsExactlyElementsOf(expectedTrainings);
        assertThat(actual.getFirst().getTrainer())
                .usingRecursiveComparison(getTrainerConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainer());
        assertThat(actual.getFirst().getTrainingType())
                .usingRecursiveComparison(getTrainingTypeConfigForDirectFields())
                .isEqualTo(expectedTrainings.getFirst().getTrainingType());
    }

}
