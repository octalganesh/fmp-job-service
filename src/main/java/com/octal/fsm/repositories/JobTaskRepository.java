package com.octal.fsm.repositories;


import com.octal.fsm.entities.JobTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobTaskRepository extends JpaRepository<JobTask, Long>, JpaSpecificationExecutor<JobTask> {

    Boolean existsByUuid(String uuid);

    Optional<JobTask> findByUuid(String id);
}
