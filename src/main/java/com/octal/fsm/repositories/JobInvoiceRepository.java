package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobInvoiceRepository extends JpaRepository<JobInvoice, Long>, JpaSpecificationExecutor<JobInvoice> {

    @Query("SELECT i FROM JobInvoice i WHERE i.deleted = false AND i.jobId IN :jobIds")
    List<JobInvoice> findByJobIdAndDeletedFalse(@Param("jobIds") List<String> jobIds);

    Optional<JobInvoice> getByUuid(String uuid);

    List<JobInvoice> findByInvoiceIdIn(List<String> invoiceIds);

    @Query(value = "SELECT JSON_ARRAYAGG(JSON_OBJECT('month', month_start, 'amount', total_amount)) " +
            "AS monthly_data FROM (SELECT DATE_FORMAT(i.created_at, '%Y-%m-01') AS month_start," +
            " SUM(i.amount) AS total_amount FROM job_invoice i WHERE i.created_at >= DATE_SUB(CURDATE(), " +
            "INTERVAL :months MONTH) GROUP BY month_start ORDER BY month_start) t", nativeQuery = true)
    Object findRevenueOverview(@Param("months") int months);

}
