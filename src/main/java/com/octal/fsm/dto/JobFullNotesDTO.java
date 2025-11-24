package com.octal.fsm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobFullNotesDTO {

    private String jobId;
    private List<JobNotesDTO> jobNotes;
    private List<TaskNotesDTO> taskNotes;

    @Data
    public static class JobNotesDTO {
        private String note;
        private LocalDateTime createdAt;
    }

    @Data
    public static class TaskNotesDTO {
        private String taskId;
        private String taskName;
        private String taskNote;
        private LocalDateTime createdAt;
        private TechnicianNotesDTO technicians; // single object, NOT list
    }


    @Data
    public static class TechnicianNotesDTO {
        private String technicianId;
        private String taskNote;
        private LocalDateTime createdAt;
    }

}
