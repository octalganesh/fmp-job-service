package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobCallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCallHistoryRepository extends JpaRepository<JobCallHistory, Long>, JpaSpecificationExecutor<JobCallHistory> {

}
