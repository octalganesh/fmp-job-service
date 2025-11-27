package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobInvoice;
import com.octal.fsm.entities.JobTaskMappingTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobTaskMappingTechnicianRepository extends JpaRepository<JobTaskMappingTechnician, Long>, JpaSpecificationExecutor<JobTaskMappingTechnician> {

    Optional<JobTaskMappingTechnician> findByJobTaskMappingId(String taskId);
    List<JobTaskMappingTechnician> findByJobTaskMappingIdIn(List<String> taskIds);

    Boolean existsByJobTaskMappingId(String taskId);

    Optional<JobTaskMappingTechnician> findByUuidAndDeletedFalse(String taskId);

    List<JobTaskMappingTechnician> findByTechnicianIdInAndDeletedFalse(List<String> technicianUuids);

    @Query("SELECT j.technicianId, SUM(CASE WHEN j.taskStatus = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount," +
            " SUM(CASE WHEN j.taskStatus NOT IN ('COMPLETED','CANCELLED') THEN 1 ELSE 0 END) AS activeCount " +
            " FROM JobTaskMappingTechnician j GROUP BY j.technicianId")
    List<Object[]> getTechnicianJobStats();

    @Query("SELECT j FROM JobTaskMappingTechnician j " +
            "WHERE j.startDate <= :today " +
            "AND j.endDate >= :today")
    List<JobTaskMappingTechnician> findActiveTasksForToday(@Param("today") LocalDate today);

    @Query("SELECT i FROM JobTaskMappingTechnician i WHERE i.deleted = false AND i.jobTaskMappingId IN :jobIds")
    List<JobTaskMappingTechnician> findByJobTaskIdAndDeletedFalse(@Param("jobIds") List<String> jobIds);

    @Query("SELECT i FROM JobTaskMappingTechnician i WHERE i.deleted = false AND i.technicianId = :technicianId")
    List<JobTaskMappingTechnician> findByTechnicianId(@Param("technicianId") String technicianId);




}
