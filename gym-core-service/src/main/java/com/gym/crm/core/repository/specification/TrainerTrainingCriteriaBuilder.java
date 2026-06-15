package com.gym.crm.core.repository.specification;

import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.entity.Trainee_;
import com.gym.crm.core.entity.Trainer_;
import com.gym.crm.core.entity.Training;
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
public class TrainerTrainingCriteriaBuilder extends TrainingCriteriaBuilder<TrainerTrainingSearchFilter> {

    @Override
    protected void fetchRequiredAssociations(Root<Training> root) {
        root.fetch(Training_.TRAINEE, JoinType.INNER).fetch(Trainee_.USER, JoinType.INNER);
        root.fetch(Training_.TRAINING_TYPE, JoinType.INNER);
    }

    @Override
    protected Join<?, User> getTargetUserJoin(Root<Training> root) {
        return root.join(Training_.TRAINER, JoinType.INNER).join(Trainer_.USER, JoinType.INNER);
    }

    @Override
    protected String getPartnerName(TrainerTrainingSearchFilter criteria) {
        return criteria.getTraineeName();
    }

    @Override
    protected Join<?, User> getPartnerUserJoin(Root<Training> root) {
        return root.join(Training_.TRAINEE, JoinType.INNER).join(Trainee_.USER, JoinType.INNER);
    }

    @Override
    protected void addSpecificFilters(CriteriaBuilder cb, Root<Training> root, TrainerTrainingSearchFilter criteria, List<Predicate> predicates) {
        // No additional filters are required as traineeName is handled by addPartnerNameFilter in the base class
    }

}
