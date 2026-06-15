package com.gym.crm.core.helper;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.User;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TestDbClient {

    private final JdbcClient jdbcClient;

    public TestDbClient(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public User findUser(Long id) {
        return jdbcClient.sql("SELECT * FROM user WHERE id = ?")
                .params(id)
                .query(User.class)
                .single();
    }

    public TrainingType findTrainingType(Long id) {
        return jdbcClient.sql("SELECT * FROM training_type WHERE id = ?")
                .params(id)
                .query(TrainingType.class)
                .single();
    }

    public Trainee findTrainee(Long id) {
        return jdbcClient.sql("SELECT * FROM trainee WHERE id = ?")
                .params(id)
                .query((rs, rowNum) -> {
                    long userId = rs.getLong("user_id");
                    return Trainee.builder()
                            .id(rs.getLong("id"))
                            .address(rs.getString("address"))
                            .dateOfBirth(rs.getDate("date_of_birth").toLocalDate())
                            .user(findUser(userId))
                            .build();
                })
                .single();
    }

    public Trainer findTrainer(Long id) {
        return jdbcClient.sql("SELECT * FROM trainer WHERE id = ?")
                .params(id)
                .query((rs, rowNum) -> {
                    long userId = rs.getLong("user_id");
                    long specializationId = rs.getLong("specialization_id");
                    return Trainer.builder()
                            .id(rs.getLong("id"))
                            .user(findUser(userId))
                            .specialization(findTrainingType(specializationId))
                            .build();
                })
                .single();
    }

    public Training findTraining(Long id) {
        return jdbcClient.sql("SELECT * FROM training WHERE id = ?")
                .params(id)
                .query((rs, rowNum) -> {
                    long traineeId = rs.getLong("trainee_id");
                    long trainerId = rs.getLong("trainer_id");
                    long trainingTypeId = rs.getLong("training_type_id");
                    return buildTraining(rs, traineeId, trainerId, trainingTypeId);
                })
                .single();
    }

    public Training findTrainingSimple(Long id) {
        return jdbcClient.sql("SELECT * FROM training WHERE id = ?")
                .params(id)
                .query(Training.class)
                .single();
    }

    public Trainee findTraineeSimple(Long id) {
        return jdbcClient.sql("SELECT * FROM trainee WHERE id = ?")
                .params(id)
                .query(Trainee.class)
                .single();
    }

    public Trainer findTrainerSimple(Long id) {
        return jdbcClient.sql("SELECT * FROM trainer WHERE id = ?")
                .params(id)
                .query(Trainer.class)
                .single();
    }

    public List<Trainer> findTraineeTrainers(Long traineeId) {
        return jdbcClient.sql("""
                        SELECT tr.* FROM trainer tr
                        INNER JOIN trainee_trainer tt ON tt.trainer_id = tr.id
                        WHERE tt.trainee_id = ?
                        """)
                .params(traineeId)
                .query(Trainer.class)
                .list();
    }

    public List<Training> findTraineeTrainings(Long traineeId) {
        return jdbcClient.sql("SELECT t.* FROM training t WHERE t.trainee_id = ?")
                .params(traineeId)
                .query(Training.class)
                .list();
    }

    public long countUsers() {
        return jdbcClient.sql("SELECT COUNT(*) FROM user")
                .query(Long.class)
                .single();
    }

    public long countTrainees() {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainee")
                .query(Long.class)
                .single();
    }

    public long countTrainers() {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainer")
                .query(Long.class)
                .single();
    }

    public long countTrainings() {
        return jdbcClient.sql("SELECT COUNT(*) FROM training")
                .query(Long.class)
                .single();
    }

    public long countTrainingTypes() {
        return jdbcClient.sql("SELECT COUNT(*) FROM training_type")
                .query(Long.class)
                .single();
    }

    public long countTraineeTrainers(Long traineeId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainee_trainer WHERE trainee_id = ?")
                .params(traineeId)
                .query(Long.class)
                .single();
    }

    public long countTrainerTrainees(Long trainerId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainee_trainer WHERE trainer_id = ?")
                .params(trainerId)
                .query(Long.class)
                .single();
    }

    public long countTraineeTrainings(Long traineeId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM training WHERE trainee_id = ?")
                .params(traineeId)
                .query(Long.class)
                .single();
    }

    public long countTrainerTrainings(Long trainerId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM training WHERE trainer_id = ?")
                .params(trainerId)
                .query(Long.class)
                .single();
    }

    public boolean userExists(String username) {
        return jdbcClient.sql("SELECT COUNT(*) FROM user WHERE username = ?")
                .params(username)
                .query(Long.class)
                .single() > 0;
    }

    public boolean traineeExists(Long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainee WHERE id = ?")
                .params(id)
                .query(Long.class)
                .single() > 0;
    }

    public boolean trainerExists(Long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM trainer WHERE id = ?")
                .params(id)
                .query(Long.class)
                .single() > 0;
    }

    private Training buildTraining(ResultSet rs, long traineeId, long trainerId, long trainingTypeId) throws SQLException {
        return Training.builder()
                .id(rs.getLong("id"))
                .trainee(findTraineeSimple(traineeId))
                .trainer(findTrainerSimple(trainerId))
                .trainingName(rs.getString("training_name"))
                .trainingType(findTrainingType(trainingTypeId))
                .trainingDate(rs.getDate("training_date").toLocalDate())
                .trainingDuration(rs.getInt("training_duration"))
                .build();
    }

}
