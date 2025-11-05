package com.octal.fsm.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "appointments")
public class Appointment extends AbstractPersistable {

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "job_type")
    private String jobTypeId;

    @Lob
    @Column(name = "job_tags")
    private String jobTags; // Comma-separated or normalized table

    @Column(name = "job_task_id")
    private String jobTaskId;

    @Column(name = "technician_id")
    private String technicianId;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "additional_notes", length = 2000)
    private String additionalNotes;

}
