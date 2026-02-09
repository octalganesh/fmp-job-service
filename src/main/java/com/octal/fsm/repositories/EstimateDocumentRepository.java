package com.octal.fsm.repositories;

import com.octal.fsm.entities.EstimateBill;
import com.octal.fsm.entities.EstimateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateDocumentRepository extends JpaRepository<EstimateDocument, Long>, JpaSpecificationExecutor<EstimateDocument> {
}
