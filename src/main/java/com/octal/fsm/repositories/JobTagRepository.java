package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobTag;
import com.octal.fsm.entities.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTagRepository extends JpaRepository<JobTag, Long>, JpaSpecificationExecutor<JobTag> {

    Optional<JobTag> findByUuid(String id);

    List<JobTag> findByUuidIn(List<String>ids);

    Boolean existsByName(String name);

    Boolean existsByUuid(String uuid);

    Boolean existsByNameAndUuidNot(String name, String id);
}
