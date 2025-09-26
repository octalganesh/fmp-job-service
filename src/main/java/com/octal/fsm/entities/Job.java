package com.octal.fsm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jobs")
@Data
public class Job extends AbstractPersistable{

    //@ManyToOne
    //@JoinColumn(name = "customer_id", nullable = false)
    //private Customer customer;

    @ManyToOne
    private JobType jobType;

    @ManyToMany
    @JoinTable(
            name = "job_jobtag",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "job_tag_id")
    )
    private Set<JobTag> tags = new HashSet<>();


    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "assigned_technician_id")
    //private Technician assignedTechnician;

    @Column(name = "job_status")
    private String jobStatus;

    @Column(name = "job_time_duration")
    private String jobTimeDuration;

    @Column(name = "job_summary")
    private String jobSummary;

    @Column(name="assigned_date_time")
    private LocalDateTime assignedDateTime;

    @Column(name="completed_date_time")
    private Boolean active;

    @Column(name="invoice_share_date")
    private LocalDate invoiceSharedDate;

    @Column(name="invoice_status")
    private String invoiceStatus;


    @Column(name="payment_status")
    private String paymentStatus;

    @Column(name = "payment_receive_date")
    private LocalDate paymentReceiveDate;

    @Column(name = "priority")
    private String priority;

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    //todo start date and end date of job
    //todo start time and end time of job

}

