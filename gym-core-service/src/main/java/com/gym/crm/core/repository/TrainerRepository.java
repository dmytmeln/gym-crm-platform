package com.gym.crm.core.repository;

import com.gym.crm.core.entity.Trainee_;
import com.gym.crm.core.entity.Trainer;
import com.gym.crm.core.entity.Trainer_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @EntityGraph(attributePaths = {
            Trainer_.USER,
            Trainer_.SPECIALIZATION,
            Trainer_.TRAINEES,
            Trainer_.TRAINEES + "." + Trainee_.USER
    })
    @Query("SELECT t FROM Trainer t WHERE t.user.username = :username")
    Optional<Trainer> findByUsernameWithUserAndTraineesDetails(String username);

    @EntityGraph(attributePaths = {Trainer_.USER})
    @Query("SELECT t FROM Trainer t WHERE t.user.username = :username")
    Optional<Trainer> findByUsernameWithUser(String username);

    @Query("SELECT t FROM Trainer t WHERE t.user.username = :username")
    Optional<Trainer> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {Trainer_.USER, Trainer_.SPECIALIZATION})
    @NonNull
    List<Trainer> findAll();

    @EntityGraph(attributePaths = {Trainer_.USER, Trainer_.SPECIALIZATION})
    @Query("SELECT t FROM Trainer t WHERE t.id NOT IN (SELECT tr.id FROM Trainee te JOIN te.trainers tr WHERE te.user.username = :traineeUsername)")
    List<Trainer> findTraineeAvailableTrainers(String traineeUsername);

    @EntityGraph(attributePaths = {Trainer_.USER, Trainer_.SPECIALIZATION})
    @Query("SELECT t FROM Trainer t WHERE t.user.username IN (:trainerUsernames)")
    List<Trainer> findTraineeTrainersByUsernames(List<String> trainerUsernames);

    @Query("SELECT COUNT(t) FROM Trainer t WHERE t.user.isActive = true")
    long countActiveTrainers();

    boolean existsByUserUsername(String username);

}
