package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public interface JobReportNativeRepository extends JpaRepository<Job, Long> {

    @Query(value =
            "SELECT " +
                    " (SELECT COUNT(*) FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate) AS total_jobs, " +
                    " (SELECT COUNT(*) FROM jobs j WHERE j.tenant_id = :tenantId AND j.job_status = 'ACTIVE' AND j.created_at BETWEEN :startDate AND :endDate) AS active_jobs, " +
                    " (SELECT COALESCE(SUM(CASE WHEN i.paid = TRUE THEN i.amount ELSE 0 END), 0) FROM job_invoice i WHERE i.created_at BETWEEN :startDate AND :endDate) AS total_revenue_collected, " +
                    " (SELECT COALESCE(SUM(i.amount), 0) FROM job_invoice i WHERE i.created_at BETWEEN :startDate AND :endDate) AS total_revenue_generated, " +
                    " (SELECT COALESCE(SUM(CASE WHEN i.paid = FALSE OR i.paid IS NULL THEN i.amount ELSE 0 END), 0) FROM job_invoice i WHERE i.created_at BETWEEN :startDate AND :endDate) AS total_pending_amounts, " +
                    " (SELECT COUNT(*) FROM job_invoice i WHERE i.created_at BETWEEN :startDate AND :endDate) AS total_invoices, " +
                    " (SELECT COUNT(*) FROM appointments a WHERE a.created_at BETWEEN :startDate AND :endDate) AS total_appointments, " +
                    // group jobs by job type name (JSON_OBJECTAGG)
                    " (SELECT JSON_OBJECTAGG(jt.name, job_count) " +
                    "   FROM (SELECT j.job_type_id, COUNT(*) AS job_count FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate GROUP BY j.job_type_id) jg " +
                    "   JOIN job_type jt ON jt._uuid = jg.job_type_id) AS jobs_per_job_type, " +
                    // top 5 job types
                    " (SELECT JSON_OBJECTAGG(jt.name, job_count) " +
                    "   FROM (SELECT j.job_type_id, COUNT(*) AS job_count FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate GROUP BY j.job_type_id ORDER BY job_count DESC LIMIT 5) jg " +
                    "   JOIN job_type jt ON jt._uuid = jg.job_type_id) AS popular_job_types, " +
                    // group jobs by service location
                    " (SELECT JSON_OBJECTAGG(service_location, job_count) " +
                    "   FROM (SELECT j.service_location, COUNT(*) AS job_count FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate GROUP BY j.service_location) js) AS jobs_per_service_location, " +
                    // top 5 service locations
                    " (SELECT JSON_OBJECTAGG(service_location, job_count) " +
                    "   FROM (SELECT j.service_location, COUNT(*) AS job_count FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate GROUP BY j.service_location ORDER BY job_count DESC LIMIT 5) js) AS popular_service_locations ",
            nativeQuery = true)
    Map<String, Object> getFullReport(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate, @Param("tenantId") Long tenantId);


    @Query(value =
            "SELECT " +
                    " (SELECT COUNT(*) FROM jobs j WHERE j.tenant_id = :tenantId AND j.job_status = 'ACTIVE' AND j.created_at BETWEEN :startDate AND :endDate) AS active_jobs, " +
                    " (SELECT COUNT(*) FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate) AS total_jobs, " +

                    // Total Revenue Generated (Paid Invoices)
                    " (SELECT COALESCE(SUM(i.amount), 0) FROM job_invoice i JOIN jobs j ON j._uuid = i.job_id " +
                    " WHERE j.tenant_id = :tenantId AND i.paid = TRUE AND i.created_at BETWEEN :startDate AND :endDate) AS total_revenue_generated, " +

                    // Total Pending Payment (Unpaid or Null)
                    " (SELECT COALESCE(SUM(i.amount), 0) FROM job_invoice i JOIN jobs j ON j._uuid = i.job_id " +
                    " WHERE j.tenant_id = :tenantId AND (i.paid = FALSE OR i.paid IS NULL) AND i.created_at BETWEEN :startDate AND :endDate) AS total_pending_payment, " +

                    // Popular Job Types (top 5)
                    " (SELECT JSON_OBJECTAGG(jg.job_type_id, jg.job_count) " +
                    " FROM (SELECT j.job_type_id, COUNT(*) AS job_count " +
                    "       FROM jobs j WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate " +
                    "       GROUP BY j.job_type_id ORDER BY job_count DESC LIMIT 5) jg) AS popular_job_types, " +

                    // ✅ Popular Service Locations (top 5) with revenue
                    " (SELECT JSON_OBJECTAGG(js.service_location, JSON_OBJECT('jobs', js.job_count, 'revenue', js.total_revenue)) " +
                    "   FROM (SELECT j.service_location, COUNT(*) AS job_count, " +
                    "                COALESCE(SUM(i.amount), 0) AS total_revenue " +
                    "         FROM jobs j " +
                    "         LEFT JOIN job_invoice i ON i.job_id = j._uuid " +
                    "         WHERE j.tenant_id = :tenantId " +
                    "           AND j.created_at BETWEEN :startDate AND :endDate " +
                    "         GROUP BY j.service_location " +
                    "         ORDER BY job_count DESC LIMIT 5) js) AS popular_service_locations, " +

                    // ✅ Job Type Counts (All)
                    " (SELECT JSON_OBJECTAGG(jt.name, jg.job_count) FROM (SELECT j.job_type_id, COUNT(*) AS job_count FROM jobs j " +
                    " WHERE j.tenant_id = :tenantId AND j.created_at BETWEEN :startDate AND :endDate GROUP BY j.job_type_id " +
                    " ORDER BY job_count DESC LIMIT 5) jg JOIN job_type jt ON jt._uuid = jg.job_type_id) AS job_type_counts ",
            nativeQuery = true)
    Map<String, Object> getJobDashboardReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("tenantId") Long tenantId
    );

}
