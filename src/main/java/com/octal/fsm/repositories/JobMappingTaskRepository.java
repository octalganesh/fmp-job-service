package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import com.octal.fsm.entities.JobMappingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMappingTaskRepository extends JpaRepository<JobMappingTask, Long>, JpaSpecificationExecutor<JobMappingTask>,JobMappingTaskCustomRepository  {
    Optional<JobMappingTask> findByUuid(String uuid);

    Boolean existsByTaskShowId(String newCode);

    Boolean existsByUuidAndDeletedFalse(String uuid);

    Optional<JobMappingTask> findByTaskShowId(String jobTaskId);

    @Query("SELECT jmt FROM JobMappingTask jmt " +
            "JOIN FETCH jmt.job " +
            "WHERE jmt.uuid = :uuid")
    Optional<JobMappingTask> findByUuidWithJob(@Param("uuid") String uuid);

    List<JobMappingTask> findByJob(Job job);

    List<JobMappingTask> findByJobOrderByTaskSequenceAsc(Job job);

    List<JobMappingTask> findByJob_Uuid(String jobUuid);

    List<JobMappingTask> findByTaskShowIdIn(List<String> taskShowId);

    List<JobMappingTask> findByUuidIn(List<String> uuids);
}
