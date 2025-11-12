package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster,Long> {

}
