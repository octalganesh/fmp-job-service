package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_task_mapping_technician")
@Data
public class JobTaskMappingTechnician extends AbstractPersistable {

    @Column(name = "job_task_mapping_id", nullable = false)
    private String jobTaskMappingId;

    @Column(name = "technician_id", nullable = false)
    private String technicianId;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "note")
    private String note;

    @Column(name = "technician_note")
    private String technicianNote;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Lob
    @Column(name = "documents")
    private String documents;

    @Column(name = "signature")
    private String signature;  // customer signature image URL when job task is completed

    @Column(name = "signature_date_time")
    private LocalDateTime signatureDateTime;

    @Column(name = "cancel_reason")
    private String cancelReason;


}

