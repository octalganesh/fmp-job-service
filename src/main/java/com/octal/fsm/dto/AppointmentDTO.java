package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentDTO {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Add {
        private String id;
        private String jobId;
        private String jobTypeId;
        private List<String> jobTags;
        private String jobTaskId;
        private String technicianId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDateTime;
        @NotNull(message = "End date and time is mandatory")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDateTime;
        private String additionalNotes;
        private String status;
        private String appointmentTypeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private String id;
        private String jobId;
        private String jobTypeId;
        private String jobTypeName;
        private List<String> jobTags;
        private String jobTaskId;
        private String technicianId;
        private String technicianName;
        private String startDateTime;
        private String endDateTime;
        private String startTime;
        private String endTime;
        private String additionalNotes;
        private Boolean isActive;
        private String status;
        private String customerName;
        private String taskName;
        private String appointmentTypeId;
        private String appointmentTypeName;
    }
}
