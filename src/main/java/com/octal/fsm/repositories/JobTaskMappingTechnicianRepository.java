package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobTag;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobTaskMappingTechnicianRepository extends JpaRepository<JobTaskMappingTechnician, Long>, JpaSpecificationExecutor<JobTaskMappingTechnician> {

    Optional<JobTaskMappingTechnician> findByJobTaskMappingId(String taskId);

    Boolean existsByJobTaskMappingId(String taskId);

    Optional<JobTaskMappingTechnician> findByUuidAndDeletedFalse(String taskId);
}
