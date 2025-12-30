package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskManagerDTO {

    private String taskStatus;
    private String taskShowId;
    private String taskName;
    private String taskId;
    private String date;
    private String startTime;
    private String endTime;
    private String description;
    private String jobId;
    private String jobUuid;
    private String technicianName;
    private String customerName;
    private String JobTypeName;


    public TaskManagerDTO(String jobId, String taskId, String taskName, String taskStatus, String createdAt) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.date = createdAt;
    }

}
