package com.octal.fsm.repositories;

import com.octal.fsm.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByUuidAndDeletedFalse(String id);

    Page<Job> findAllByDeletedFalse(Pageable pageable);

    //List<Job> findByCustomerIdAndDeletedFalse(Long customerId);
}
