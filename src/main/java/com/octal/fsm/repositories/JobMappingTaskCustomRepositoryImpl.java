package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobMappingTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.List;

@Repository
public class JobMappingTaskCustomRepositoryImpl implements JobMappingTaskCustomRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<JobMappingTask> findAllWithJobAndTags(Specification<JobMappingTask> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<JobMappingTask> query = cb.createQuery(JobMappingTask.class);
        Root<JobMappingTask> root = query.from(JobMappingTask.class);

        // APPLY SPECIFICATION
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            query.where(predicate);
        }

        // APPLY FETCH JOINS ONLY FOR THIS METHOD
        root.fetch("job", JoinType.LEFT).fetch("jobMappingTags", JoinType.LEFT);

        query.distinct(true);

        return entityManager.createQuery(query).getResultList();
    }
}
