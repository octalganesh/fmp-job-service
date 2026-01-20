package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Long> {

    Optional<JobStatusMaster> findByUuid(String statusMasterId);

    List<JobStatusMaster> findAllByTenantIdAndDeletedFalse(Long tenantIdToUse);

    @Query(
            value = " SELECT * FROM job_status_master j WHERE j.is_deleted = false AND j.record_id IN ( SELECT MIN(record_id) FROM job_status_master WHERE is_deleted = false GROUP BY name)",
            nativeQuery = true
    )
    List<JobStatusMaster> findDistinctNamesByDeletedFalse();

    Optional<JobStatusMaster> findBySequenceOrderAndTenantId(Integer sequenceOrder, Long tenant);

    List<JobStatusMaster> findByName(String name);

}
