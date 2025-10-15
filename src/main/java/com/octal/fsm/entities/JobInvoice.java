package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_invoice")
@Data
public class JobInvoice extends AbstractPersistable {

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "invoice_id", unique = true, nullable = false)
    private String invoiceId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "send_on_email", nullable = false)
    private String sendOnEmail;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "note")
    private String note;

    @Column(name = "invoice_type", nullable = false)
    private String invoiceType; // UPFRONT, FINAL

    @Lob
    @Column(name = "request_dto", nullable = false)
    private String requestDTO;

    @Lob
    @Column(name = "response_dto", nullable = false)
    private String responseDTO;

    @Column(name = "paid", nullable = false)
    private Boolean paid = false;
}
