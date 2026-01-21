package com.octal.fsm.repositories;

import com.octal.fsm.entities.Appointment;
import com.octal.fsm.entities.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long>{

    Optional<AppointmentType> findByUuid(String id);

}
