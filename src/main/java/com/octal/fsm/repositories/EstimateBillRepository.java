package com.octal.fsm.repositories;

import com.octal.fsm.entities.EstimateBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstimateBillRepository extends JpaRepository<EstimateBill, Long>, JpaSpecificationExecutor<EstimateBill> {

    boolean existsByEstimateId(String estimateId);

    Optional<EstimateBill> findByEstimateIdAndTenantId(String estimateId,Long tenantId);

    @Query(value = "SELECT estimate_id FROM estimates WHERE estimate_id LIKE :prefix% ORDER BY estimate_id DESC LIMIT 1", nativeQuery = true)
    String findLastEstimateId(@Param("prefix") String prefix);

}
