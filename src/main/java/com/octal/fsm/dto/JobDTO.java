package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.octal.fsm.dto.enums.JobUpdateType;
import com.octal.fsm.entities.JobMappingTask;
import com.octal.fsm.entities.JobStatusMaster;
import com.octal.fsm.entities.enums.TaskAssignedType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JobDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {

        private String jobUuiId;
        private CustomerDetails customerDetails;
        private String frontOfficeId;

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
        private String uploadedByType; // Admin, FrontOffice, Technician
        private String uploadedByTypeId; // Admin Id, Customer Id, Technician Id
        private String uploadedByUserName; // Admin Id, Customer Id, Technician Id
        private Long tenantId;
        private Boolean isSuperAdmin;
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
        private String customerQuickBookId;
    }

    @Data
    public static class CreateUpFrontInvoiceRequest {
        private String jobId;
        private Double amount;
        private String email;
        private String dueDate;
        private String note;
        private String invoiceType;
    }

    @Data
    public static class LeaveJob {
        private String jobId;
        private String frontOfficeUserId;
        private String frontOfficeUserName;
        private String reasonForLeave;
        private JobUpdateType jobUpdateType;

    }

    @Data
    public static class UpdateJobTags {
        private List<String> jobTags;
    }

    @Data
    public static class UpdateAssignedTaskWithDocumentType {
        private List<String> documentTypeId;
    }

    @Data
    public static class InvoiceListResponse {
        private String id;
        private String invoiceId;
        private String invoiceType;
        private Double amount;
        private String sendOnEmail;
        private String dueDate;
        private String note;
        private String createdAt;
        private Boolean paid;
        private String jobId;
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
        private String customerType;
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
        private String startDateTime;
        private String endDateTime;
        private String taskShowId;
        private String taskName;
    }

    @Data
    public static class JobTaskListResponse {
        private String id;
        private String taskId;
        private String taskShowId;
        private String taskName;
        private String taskDescription;
        private String createdAt;
        private String taskStatus;
        private String technicianName;
        private String technicianId;
        private TaskAssignedType assignedType;
        private Integer sequenceNumber;
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
        private String frontOfficeId;
        private JobStatusMaster jobStatusMaster;
        private String currentTaskId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseForTechnician {
        String id;
        String jobId;
        String title;
        String status;
        String startDate;
        String endDate;
        String startTime;
        String endTime;
        String customerName;
        String location;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobFilterRequest {
        private Integer page;
        private Integer limit;
        private String jobDate;
        private String txt;
        private String status;
        private List<String> jobType;
        private List<String> jobTag;
        private String startDate;
        private String endDate;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobHistoryDTO {

        private Long recordId;
        private String id;
        private Long tenantId;
        private String frontOfficeId;
        private String reason;
        private String jobId;
        private String frontOfficeName;
        private Boolean isActive;
        private boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailsForTechnician {
        private String id;
        private String taskId;
        private String taskName;
        private String note;
        private String frontOfficeNote;
        private String startDate;
        private String endDate;
        private String taskDescription;
        private String clientFeedbackUrl;
        private String signature;
        private String cancelReason;
        private String drawingJsonData;
        private String drawingImage;
        // Customer Info
        private String customerId;
        private String customerName;
        private String email;
        private String mobileNumber;
        private String location;
        // Job Info
        private String jobTitle;
        private String jobId;
        private String jobType;
        private String jobStartDate;
        private String jobEndDate;
        private String jobNote;
        private List<Document> jobUploadedDocuments = new ArrayList<>();
        // Tags, Documents, Description
        private List<String> jobTags;
        private List<Document> uploadedDocuments;
        private String jobDescription;
        private String status;
        private Double serviceLocationLat;
        private Double serviceLocationLng;
        // response for customer feedback on the task
        private CustomerFeedbackResponse customerFeedbackResponse;
        private List<HTMLFormDTO.Details> formList;
        private String frontOfficeName;
        private String frontOfficeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechnicianForFrontOffice{
        private String id;
        private String taskId;
        private String taskName;
        private String note;
        private String frontOfficeNote;
        private String startDate;
        private String endDate;
        private String startTime;
        private String endTime;
        private String taskDescription;
        private String clientFeedbackUrl;
        private String signature;
        private String cancelReason;
        private String drawingJsonData;
        private String drawingImage;
        private String customerId;
        private List<String> jobTags;
        private List<Document> jobUploadedDocuments = new ArrayList<>();
        private List<Document> uploadedDocuments;
        private String jobDescription;
        private String status;
        private Double serviceLocationLat;
        private Double serviceLocationLng;
        private String frontOfficeName;
        private String frontOfficeId;
        private List<HTMLFormDTO.Details> formList;
        private String assignType;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Document {
        private String file;
        private String fileType;
        private String fileName;
        private String thumbnail;
        private String documentTypeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerFeedbackResponse {
        private String id;
        private String customerName;
        private String feedback;
        private Double rating;
        private String jobId;
        private String createdAt;
        private String jobTaskId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskDrawingRequest {
        private String drawingJson;
        private String drawingFileUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddJobStatus {
        private String name;
        private Integer sequenceOrder;
        private String colorCode; // todo need to discus
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobStatusDetail {
        private String id;
        private String name;
        private String colorCode; // todo need to discus
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateJobTaskDetails {
        private String taskId;
        private String note;
        private TaskAssignedType assignedType;
        private List<DocumentDTO.Add> documents;
        private Boolean isDone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateDrawingDetails {
        private String taskId;
        private String drawingJsonData;
        private String drawingImage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobCustomerDTO {
        private String jobType;
        private String jobId;
        private String id;
        private String status;
        private String createdAt;
        private String startDate;
        private String endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobMappingTaskTechnician {
        private String jobType;
        private String jobId;
        private String id;
        private String taskStatus;
        private String createdAt;
        private String taskShowId;
        private String taskName;
    }

}
