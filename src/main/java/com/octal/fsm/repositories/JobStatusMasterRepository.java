package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Long> {

    Optional<JobStatusMaster> findByUuid(String statusMasterId);

    List<JobStatusMaster> findAllByTenantIdAndDeletedFalse(Long tenantIdToUse);

}
