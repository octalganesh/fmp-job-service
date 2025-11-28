package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobTask;
import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long>, JpaSpecificationExecutor<JobType> {

    Optional<JobType> findByUuid(String id);

    Optional<JobType> findByUuidAndTenantId(String id, Long tenantId);

    @Query("SELECT j FROM JobType j WHERE j.deleted = false AND j.uuid IN :uuids")
    List<JobType> findByUuidAndDeletedFalse(@Param("uuids") List<String> uuids);
}
