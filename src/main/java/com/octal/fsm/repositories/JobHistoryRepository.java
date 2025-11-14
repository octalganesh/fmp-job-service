package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long>, JpaSpecificationExecutor<JobHistory> {

}
