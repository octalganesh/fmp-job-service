package com.octal.fsm.repositories;

import com.octal.fsm.entities.SystemEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemEventLogRepository extends JpaRepository<SystemEventLog, Long>, JpaSpecificationExecutor<SystemEventLog> {

    List<SystemEventLog> findByReferenceIdOrderByCreatedAtDesc(String referenceId);
}
