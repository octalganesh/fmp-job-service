package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskManagerDTO {

    private String taskStatus;
    private String taskName;
    private String taskId;
    private String date;
    private String time;
    private String description;
    private String jobId;


    public TaskManagerDTO(String jobId, String taskId, String taskName, String taskStatus, String createdAt) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.date = createdAt;
    }

}
