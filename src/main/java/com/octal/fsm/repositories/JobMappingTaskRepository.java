package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobMappingTask;
import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobMappingTaskRepository extends JpaRepository<JobMappingTask, Long>, JpaSpecificationExecutor<JobMappingTask> {
    Optional<JobMappingTask> findByUuid(String uuid);

    Boolean existsByTaskShowId(String newCode);
}
