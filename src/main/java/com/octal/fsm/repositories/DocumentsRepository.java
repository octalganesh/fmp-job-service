package com.octal.fsm.repositories;

import com.octal.fsm.entities.Documents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentsRepository extends JpaRepository<Documents, Long>, JpaSpecificationExecutor<Documents> {
    List<Documents> findByAttachTypeId(String uuid);

    List<Documents> findByAttachTypeIdOrderByCreatedAtDesc(String attachTypeId);
}
