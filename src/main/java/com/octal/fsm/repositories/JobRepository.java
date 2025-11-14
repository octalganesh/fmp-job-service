package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByUuidAndDeletedFalse(String id);
    Optional<Job> findByUuidAndTenantIdAndDeletedFalse(String id,Long tenantId);

    Page<Job> findAllByDeletedFalse(Pageable pageable);

    Boolean existsByJobId(String jobId);

    Boolean existsByUuidAndDeletedFalse(String uuid);
    Boolean existsByUuidAndTenantIdAndDeletedFalse(String uuid,Long tenantId);


      Optional<Job> findByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(String id,Long tenantId,String frontOfficeId);

    Optional<Job> findByUuidAndFrontOfficeIdAndDeletedFalse(String uuid, String frontOfficeId);

    Page<Job> findAllByFrontOfficeIdAndDeletedFalse(String frontOfficeId, Pageable pageable);

    boolean existsByJobIdAndFrontOfficeId(String jobId, String frontOfficeId);

    boolean existsByUuidAndFrontOfficeIdAndDeletedFalse(String uuid, String frontOfficeId);

    boolean existsByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(String uuid, Long tenantId, String frontOfficeId);
}
