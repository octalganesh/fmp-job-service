package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jobs")
@Data
public class Job extends AbstractPersistable {

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "job_type_id")
    private String jobTypeId;

    @Column(name = "customer_type_id")
    private String customerTypeId;

    @Column(name = "service_location")
    private String serviceLocation;

    @Column(name = "service_location_lat")
    private Double serviceLocationLat;

    @Column(name = "service_location_lng")
    private Double serviceLocationLng;

    @Column(name = "jobStatus", nullable = false)
    private String jobStatus;

    @Column(name = "job_description")
    private String jobDescription;

    // ✅ One-to-Many relationship with JobMappingTask
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobMappingTask> jobMappingTasks = new ArrayList<>();

    @Column(name = "lead_received_date")
    private LocalDate leadReceivedDate;

    @Column(name = "job_start_date")
    private LocalDate jobStartDate;

    @Column(name = "job_end_date")
    private LocalDate jobEndDate;

    @Column(name = "lead_source_id")
    private String leadSourceId;

    @Column(name = "budget")
    private Double budget;

    // ✅ One-to-Many relationship with JobMappingTask
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobMappingTags> jobMappingTags = new ArrayList<>();

    @Column(name = "additional_notes")
    private String additionalNotes; // Optional

    @Column(name = "job_id",unique = true,nullable = false)
    private String jobId;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobMappingDocuments> jobMappingDocuments = new ArrayList<>();
}

