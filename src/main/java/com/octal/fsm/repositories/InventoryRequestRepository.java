package com.octal.fsm.repositories;

import com.octal.fsm.entities.InventoryRequest;
import com.octal.fsm.entities.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long>, JpaSpecificationExecutor<InventoryRequest> {

    Optional<InventoryRequest> findByUuid(String uuid);

    List<InventoryRequest> findByTaskId(String taskId);

    Boolean existsByRequestShowId(String requestShowId);


}
