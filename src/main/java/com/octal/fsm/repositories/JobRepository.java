package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByUuidAndDeletedFalse(String id);

    Optional<Job> findByUuidAndTenantIdAndDeletedFalse(String id, Long tenantId);

    Page<Job> findAllByDeletedFalse(Pageable pageable);

    Boolean existsByJobId(String jobId);

    Boolean existsByUuidAndDeletedFalse(String uuid);

    Boolean existsByJobIdAndDeletedFalse(String uuid);

    Boolean existsByUuidAndTenantIdAndDeletedFalse(String uuid, Long tenantId);


    Optional<Job> findByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(String id, Long tenantId, String frontOfficeId);

    Optional<Job> findByUuidAndFrontOfficeIdAndDeletedFalse(String uuid, String frontOfficeId);

    Page<Job> findAllByFrontOfficeIdAndDeletedFalse(String frontOfficeId, Pageable pageable);

    boolean existsByJobIdAndFrontOfficeId(String jobId, String frontOfficeId);

    boolean existsByUuidAndFrontOfficeIdAndDeletedFalse(String uuid, String frontOfficeId);

    boolean existsByUuidAndTenantIdAndFrontOfficeIdAndDeletedFalse(String uuid, Long tenantId, String frontOfficeId);

    @Query("SELECT j FROM Job j WHERE j.uuid IN :uuids")
    List<Job> findByUuidIn(@Param("uuids") List<String> uuids);

    List<Job> findByJobIdIn(List<String> jobIds);

    List<Job> findByCustomerId(String customerId);

    List<Job> findByFrontOfficeIdAndDeletedFalse(String frontOfficeId);

    @Query("SELECT MIN(j.createdAt) FROM Job j WHERE j.tenantId = :tenantId")
    LocalDateTime findEarliestJobCreatedAtByTenantId(@Param("tenantId") Long tenantId);

    List<Job> findAllByFrontOfficeIdAndDeletedFalse(String frontOfficeId);

    @Query("select j from Job j left join fetch j.jobMappingTasks where j.uuid = :uuid")
    Optional<Job> findByIdWithTasks(@Param("uuid") String uuid);

}
