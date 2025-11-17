package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long>, JpaSpecificationExecutor<JobType> {

    Optional<JobType> findByUuid(String id);

    Optional<JobType> findByUuidAndTenantId(String id, Long tenantId);
}
