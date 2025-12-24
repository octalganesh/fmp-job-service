package com.octal.fsm.repositories;

import com.octal.fsm.entities.InventoryPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryPartRepository extends JpaRepository<InventoryPart, Long>, JpaSpecificationExecutor<InventoryPart> {

    Optional<InventoryPart> findByListId(String listId);
}
