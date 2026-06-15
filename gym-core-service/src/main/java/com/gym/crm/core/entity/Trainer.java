package com.gym.crm.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainer")
@Getter
@ToString(exclude = {"user", "specialization", "trainings", "trainees"})
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    @Setter
    private TrainingType specialization;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = Training_.TRAINER, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(mappedBy = Trainee_.TRAINERS, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Trainee> trainees = new HashSet<>();

    public void addTraining(Training training) {
        trainings.add(training);
        training.setTrainer(this);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
        training.setTrainer(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainer other)) return false;

        return id != null && Objects.equals(this.getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

}
