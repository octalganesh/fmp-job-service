package com.octal.fsm.repositories;

import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.entities.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long>, JpaSpecificationExecutor<InventoryRequest> {
}
