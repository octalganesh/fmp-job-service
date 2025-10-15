package com.octal.fsm.repositories;

import com.octal.fsm.entities.JobInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobInvoiceRepository  extends JpaRepository<JobInvoice, Long>, JpaSpecificationExecutor<JobInvoice> {
}
