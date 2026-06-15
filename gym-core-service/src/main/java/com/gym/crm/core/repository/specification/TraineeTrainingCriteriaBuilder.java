package com.gym.crm.core.repository.specification;

import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee_;
import com.gym.crm.core.entity.Trainer_;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.TrainingType;
import com.gym.crm.core.entity.TrainingType_;
import com.gym.crm.core.entity.Training_;
import com.gym.crm.core.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TraineeTrainingCriteriaBuilder extends TrainingCriteriaBuilder<TraineeTrainingSearchFilter> {

    @Override
    protected void fetchRequiredAssociations(Root<Training> root) {
        root.fetch(Training_.TRAINER, JoinType.INNER).fetch(Trainer_.USER, JoinType.INNER);
        root.fetch(Training_.TRAINING_TYPE, JoinType.INNER);
    }

    @Override
    protected void addSpecificFilters(CriteriaBuilder cb, Root<Training> root, TraineeTrainingSearchFilter criteria, List<Predicate> predicates) {
        addTrainingTypeNameFilter(cb, root, criteria, predicates);
    }

    @Override
    protected Join<?, User> getTargetUserJoin(Root<Training> root) {
        return root.join(Training_.TRAINEE, JoinType.INNER).join(Trainee_.USER, JoinType.INNER);
    }

    @Override
    protected String getPartnerName(TraineeTrainingSearchFilter criteria) {
        return criteria.getTrainerName();
    }

    @Override
    protected Join<?, User> getPartnerUserJoin(Root<Training> root) {
        return root.join(Training_.TRAINER, JoinType.INNER).join(Trainer_.USER, JoinType.INNER);
    }

    private void addTrainingTypeNameFilter(CriteriaBuilder cb, Root<Training> root, TraineeTrainingSearchFilter criteria, List<Predicate> predicates) {
        if (criteria.getTrainingTypeName() == null || criteria.getTrainingTypeName().isBlank()) {
            return;
        }

        Join<Training, TrainingType> trainingTypeJoin = root.join(Training_.TRAINING_TYPE, JoinType.INNER);
        predicates.add(cb.equal(trainingTypeJoin.get(TrainingType_.TRAINING_TYPE_NAME), criteria.getTrainingTypeName()));
    }

}
