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
public class Job extends AbstractPersistable {

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_quick_book_id", nullable = false)
    private String customerQuickBookId;

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

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "front_office_id")
    private String frontOfficeId;

    public String getFrontOfficeId() {
        return frontOfficeId;
    }

    public void setFrontOfficeId(String frontOfficeId) {
        this.frontOfficeId = frontOfficeId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "Job{" +
                "customerId='" + customerId + '\'' +
                ", customerQuickBookId='" + customerQuickBookId + '\'' +
                ", jobTypeId='" + jobTypeId + '\'' +
                ", customerTypeId='" + customerTypeId + '\'' +
                ", serviceLocation='" + serviceLocation + '\'' +
                ", serviceLocationLat=" + serviceLocationLat +
                ", serviceLocationLng=" + serviceLocationLng +
                ", jobStatus='" + jobStatus + '\'' +
                ", jobDescription='" + jobDescription + '\'' +
                ", jobMappingTasks=" + jobMappingTasks +
                ", leadReceivedDate=" + leadReceivedDate +
                ", jobStartDate=" + jobStartDate +
                ", jobEndDate=" + jobEndDate +
                ", leadSourceId='" + leadSourceId + '\'' +
                ", budget=" + budget +
                ", jobMappingTags=" + jobMappingTags +
                ", additionalNotes='" + additionalNotes + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobMappingDocuments=" + jobMappingDocuments +
                '}';
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerQuickBookId() {
        return customerQuickBookId;
    }

    public void setCustomerQuickBookId(String customerQuickBookId) {
        this.customerQuickBookId = customerQuickBookId;
    }

    public String getJobTypeId() {
        return jobTypeId;
    }

    public void setJobTypeId(String jobTypeId) {
        this.jobTypeId = jobTypeId;
    }

    public String getCustomerTypeId() {
        return customerTypeId;
    }

    public void setCustomerTypeId(String customerTypeId) {
        this.customerTypeId = customerTypeId;
    }

    public String getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(String serviceLocation) {
        this.serviceLocation = serviceLocation;
    }

    public Double getServiceLocationLat() {
        return serviceLocationLat;
    }

    public void setServiceLocationLat(Double serviceLocationLat) {
        this.serviceLocationLat = serviceLocationLat;
    }

    public Double getServiceLocationLng() {
        return serviceLocationLng;
    }

    public void setServiceLocationLng(Double serviceLocationLng) {
        this.serviceLocationLng = serviceLocationLng;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public List<JobMappingTask> getJobMappingTasks() {
        return jobMappingTasks;
    }

    public void setJobMappingTasks(List<JobMappingTask> jobMappingTasks) {
        this.jobMappingTasks = jobMappingTasks;
    }

    public LocalDate getLeadReceivedDate() {
        return leadReceivedDate;
    }

    public void setLeadReceivedDate(LocalDate leadReceivedDate) {
        this.leadReceivedDate = leadReceivedDate;
    }

    public LocalDate getJobStartDate() {
        return jobStartDate;
    }

    public void setJobStartDate(LocalDate jobStartDate) {
        this.jobStartDate = jobStartDate;
    }

    public LocalDate getJobEndDate() {
        return jobEndDate;
    }

    public void setJobEndDate(LocalDate jobEndDate) {
        this.jobEndDate = jobEndDate;
    }

    public String getLeadSourceId() {
        return leadSourceId;
    }

    public void setLeadSourceId(String leadSourceId) {
        this.leadSourceId = leadSourceId;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public List<JobMappingTags> getJobMappingTags() {
        return jobMappingTags;
    }

    public void setJobMappingTags(List<JobMappingTags> jobMappingTags) {
        this.jobMappingTags = jobMappingTags;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<JobMappingDocuments> getJobMappingDocuments() {
        return jobMappingDocuments;
    }

    public void setJobMappingDocuments(List<JobMappingDocuments> jobMappingDocuments) {
        this.jobMappingDocuments = jobMappingDocuments;
    }

    public JobStatusMaster getJobStatusMaster() {
        return jobStatusMaster;
    }

    public void setJobStatusMaster(JobStatusMaster jobStatusMaster) {
        this.jobStatusMaster = jobStatusMaster;
    }

    public String getCurrentTaskId() {
        return currentTaskId;
    }

    public void setCurrentTaskId(String currentTask) {
        this.currentTaskId = currentTask;
    }

    // ✅ One-to-Many relationship with JobMappingTask
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobMappingTags> jobMappingTags = new ArrayList<>();

    @Column(name = "additional_notes")
    private String additionalNotes; // Optional

    @Column(name = "job_id", unique = true, nullable = false)
    private String jobId;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobMappingDocuments> jobMappingDocuments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "job_status_id",nullable = false)  // todo need to add nullable = false later
    private JobStatusMaster jobStatusMaster;

    @Column(name="current_task_Id") // todo need to attach this as job mapping task uuid to track the job's current task
    private String currentTaskId;

}

