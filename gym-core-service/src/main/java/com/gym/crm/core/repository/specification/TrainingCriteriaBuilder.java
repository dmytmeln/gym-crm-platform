package com.gym.crm.core.repository.specification;

import com.gym.crm.core.dto.filter.TrainingSearchFilter;
import com.gym.crm.core.entity.Training;
import com.gym.crm.core.entity.Training_;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.entity.User_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public abstract class TrainingCriteriaBuilder<C extends TrainingSearchFilter> {

    public Specification<Training> build(C criteria) {
        return (root, query, cb) -> {
            if (query != null && !isCountQuery(query)) {
                fetchRequiredAssociations(root);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            addFromDateFilter(cb, root, criteria, predicates);
            addToDateFilter(cb, root, criteria, predicates);
            addUsernameFilter(cb, root, criteria, predicates);
            addPartnerNameFilter(cb, root, criteria, predicates);
            addSpecificFilters(cb, root, criteria, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected abstract void fetchRequiredAssociations(Root<Training> root);

    protected abstract Join<?, User> getTargetUserJoin(Root<Training> root);

    protected abstract String getPartnerName(C criteria);

    protected abstract Join<?, User> getPartnerUserJoin(Root<Training> root);

    protected abstract void addSpecificFilters(CriteriaBuilder cb, Root<Training> root, C criteria, List<Predicate> predicates);

    private boolean isCountQuery(CriteriaQuery<?> query) {
        return query.getResultType() == Long.class || query.getResultType() == long.class;
    }

    private void addFromDateFilter(CriteriaBuilder cb, Root<Training> root, C criteria, List<Predicate> predicates) {
        if (criteria.getFromDate() == null) {
            return;
        }

        predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.TRAINING_DATE), criteria.getFromDate()));
    }

    private void addToDateFilter(CriteriaBuilder cb, Root<Training> root, C criteria, List<Predicate> predicates) {
        if (criteria.getToDate() == null) {
            return;
        }

        predicates.add(cb.lessThanOrEqualTo(root.get(Training_.TRAINING_DATE), criteria.getToDate()));
    }

    private void addUsernameFilter(CriteriaBuilder cb, Root<Training> root, C criteria, List<Predicate> predicates) {
        if (criteria.getUsername() == null) {
            return;
        }

        Join<?, User> userJoin = getTargetUserJoin(root);
        predicates.add(cb.equal(userJoin.get(User_.USERNAME), criteria.getUsername()));
    }

    private void addPartnerNameFilter(CriteriaBuilder cb, Root<Training> root, C criteria, List<Predicate> predicates) {
        String partnerName = getPartnerName(criteria);
        if (partnerName == null) {
            return;
        }

        Join<?, User> partnerUserJoin = getPartnerUserJoin(root);
        Expression<String> fullNameExpr = concatFirstnameAndLastname(cb, partnerUserJoin);
        predicates.add(cb.equal(fullNameExpr, partnerName));
    }

    private Expression<String> concatFirstnameAndLastname(CriteriaBuilder cb, Join<?, User> userJoin) {
        return cb.concat(cb.concat(userJoin.get(User_.FIRST_NAME), " "),
                userJoin.get(User_.LAST_NAME));
    }

}
