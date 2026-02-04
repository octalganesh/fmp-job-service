package com.octal.fsm.repositories;

import com.octal.fsm.entities.CreateAssemblyQueue;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreateAssemblyQueueRepository extends JpaRepository<CreateAssemblyQueue, Long> {

    Optional<CreateAssemblyQueue> findByUuid(String uuid);

    boolean existsByAssemblyName(String name);

}
