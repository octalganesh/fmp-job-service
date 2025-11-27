package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobMappingTask;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface JobMappingTaskCustomRepository {

    List<JobMappingTask> findAllWithJobAndTags(Specification<JobMappingTask> spec);
}
