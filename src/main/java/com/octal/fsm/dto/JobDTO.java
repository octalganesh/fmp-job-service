package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class JobDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        // Job Information
        @NotBlank(message = "Job summary is required")
        private String jobSummary;

        @NotNull(message = "Job type is required")
        private String jobTypeId;

        @NotBlank(message = "Priority is required")
        private String priority;

        private Double estimatedCost;

        private Set<String> tagIds;

        // Customer Information
        @NotNull(message = "Customer is required")
        private String customerId;

        // Technician & Scheduling
        private String assignedTechnicianId;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime assignedDateTime;

        private String jobTimeDuration;

        // Additional fields
        private String jobStatus = "SCHEDULED"; // Default status

        private Boolean active = true;
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class List {
        private String id;
        private String jobSummary;
        private String jobStatus;
        private String priority;
        private Double estimatedCost;

        // Customer Information
        private String customerId;
        private String customerName;

        // Job Type
        private String jobTypeId;
        private String jobTypeName;

        // Technician Information
        private String assignedTechnicianId;
        private String assignedTechnicianName;

        // Dates
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime assignedDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate invoiceSharedDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paymentReceiveDate;

        // Status information
        private String invoiceStatus;
        private String paymentStatus;
        private Boolean active;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        // Tags
        private Set<String> tags;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private String id;
        private String jobSummary;
        private String jobStatus;
        private String priority;
        private Double estimatedCost;
        private String jobTimeDuration;

        // Customer Information
        //private CustomerDTO.list customer;

        // Job Type
        private JobTypeDTO.Detail jobType;

        // Technician Information
        //private TechnicianDto.list assignedTechnician;

        // Dates
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime assignedDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate invoiceSharedDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paymentReceiveDate;

        // Status information
        private String invoiceStatus;
        private String paymentStatus;
        private Boolean active;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        // Tags
        private Set<JobTagDTO.Detail> tags;
    }
}
