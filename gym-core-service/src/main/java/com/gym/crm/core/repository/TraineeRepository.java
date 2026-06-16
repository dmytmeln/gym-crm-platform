package com.gym.crm.core.repository;

import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.entity.Trainee_;
import com.gym.crm.core.entity.Trainer_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @EntityGraph(attributePaths = {
            Trainee_.USER,
            Trainee_.TRAINERS,
            Trainee_.TRAINERS + "." + Trainer_.USER,
            Trainee_.TRAINERS + "." + Trainer_.SPECIALIZATION
    })
    @Query("SELECT t FROM Trainee t WHERE t.user.username = :username")
    Optional<Trainee> findByUsernameWithUserAndTrainersDetails(String username);

    @EntityGraph(attributePaths = {Trainee_.USER})
    @Query("SELECT t FROM Trainee t WHERE t.user.username = :username")
    Optional<Trainee> findByUsernameWithUser(String username);

    @Query("SELECT t FROM Trainee t WHERE t.user.username = :username")
    Optional<Trainee> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {
            Trainee_.USER,
            Trainee_.TRAINERS,
            Trainee_.TRAINERS + "." + Trainer_.USER,
            Trainee_.TRAINERS + "." + Trainer_.SPECIALIZATION
    })
    @NonNull
    List<Trainee> findAll();

    boolean existsByUserUsername(String username);

    @Query("SELECT COUNT(t) FROM Trainee t WHERE t.user.isActive = true")
    long countActiveTrainees();

}
