package com.octal.fsm.repositories;

import com.octal.fsm.entities.HTMLFormPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HTMLFormPageRepository extends JpaRepository<HTMLFormPage,Long>, JpaSpecificationExecutor<HTMLFormPage> {

    Optional<HTMLFormPage> findByUuidAndDeletedFalse(String uuid);

}
