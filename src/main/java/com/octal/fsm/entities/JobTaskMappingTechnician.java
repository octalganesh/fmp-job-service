package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Lob
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

    @Lob
    @Column(name = "drawing_json")
    private String drawingJson;

    @Lob
    @Column(name = "drawing_image")
    private String drawingImage;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "job_task_mapping_technician_id")
    private List<HTMLFormPage> htmlFormPages = new ArrayList<>();

    @Column(name = "start_time")
    private Time startTime;

    @Column(name = "end_time")
    private Time endTime;


}

