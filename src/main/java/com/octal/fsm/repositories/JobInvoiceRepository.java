package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobInvoiceRepository extends JpaRepository<JobInvoice, Long>, JpaSpecificationExecutor<JobInvoice> {

    @Query("SELECT i FROM JobInvoice i WHERE i.deleted = false AND i.jobId IN :jobIds")
    List<JobInvoice> findByJobIdAndDeletedFalse(@Param("jobIds") List<String> jobIds);

    Optional<JobInvoice> getByUuid(String uuid);
}
