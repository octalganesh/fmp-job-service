package com.octal.fsm.repositories;


import com.octal.fsm.entities.JobNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobNotesRepository extends JpaRepository<JobNotes, Long>, JpaSpecificationExecutor<JobNotes> {

    List<JobNotes> findByJobId(String jobId);
}
