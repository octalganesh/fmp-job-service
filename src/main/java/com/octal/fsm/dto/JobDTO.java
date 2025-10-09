package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class JobDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {

        private CustomerDetails customerDetails;

        private String jobTypeId;
        private String serviceLocation;
        private Double serviceLocationLat;
        private Double serviceLocationLng;
        private String jobStatus;
        private String customerTypeId;
        private String jobDescription;
        private java.util.List<String> jobTaskId;
        private String leadReceivedDate;
        private String jobStartDate;
        private String jobEndDate;
        private String leadSourceId;
        private Double budget;
        private java.util.List<String> jobTags;
        private String additionalNotes; // Optional
        private java.util.List<String> documents; //Optional
    }

    @Data
    public static class CustomerDetails {
        private String customerId; // If existing customer
        private String customerName;
        private String email;
        private String mobileNumber;
        private String address;
        private Double lat;
        private Double lng;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Update {
        @NotNull(message = "Job ID is required")
        private String id;

        // Job Information
        private String jobSummary;
        private String jobTypeId;
        private String priority;
        private Double estimatedCost;
        private Set<String> tagIds;

        // Customer Information
        private String customerId;

        // Technician & Scheduling
        private String assignedTechnicianId;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime assignedDateTime;

        private String jobTimeDuration;
        private String jobStatus;

        // Invoice and Payment
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate invoiceSharedDate;

        private String invoiceStatus;
        private String paymentStatus;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paymentReceiveDate;

        private Boolean active;
    }

    @Data
    public static class JobListResponse {
        private String id;
        private String jobId;
        private String serviceLocation;
        private String jobType;
        private String customerName;
        private String jobStartDate;
        private String jobEndDate;
        private String leadSource;
        private String jobStatus;
    }

    @Data
    public static class AssignJobToTechnician {
        private String jobId;
        private String jobTaskMappingId;
        private String technicianId;
        private String note;
        private List<String> documents; // Optional
        private String startDate;
        private String endDate;
    }

    @Data
    public static class JobTaskListResponse {
        private String id;
        private String taskId;
        private String taskName;
        private String taskDescription;
        private String createdAt;
        private String taskStatus;
        private String technicianName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private CustomerDetails customerDetails;
        private String id;
        private String jobId;
        private String jobTypeId;
        private String jobType;
        private String leadSourceId;
        private String leadSource;
        private String leadReceivedDate;
        private List<JobTagDTO.Detail> jobTags;
        private String jobDescription;
        private String additionalNotes;
        private String jobStartDate;
        private String jobEndDate;
        private String serviceLocation;
        private Double serviceLocationLat;
        private Double serviceLocationLng;
        private String jobStatus;
        private String customerType;
        private String customerTypeId;
    }
}
