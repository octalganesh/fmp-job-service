package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobCallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobCallHistoryRepository extends JpaRepository<JobCallHistory, Long>, JpaSpecificationExecutor<JobCallHistory> {

    List<JobCallHistory> findByJobIdOrderByCreatedAtDesc(String jobId);
}
