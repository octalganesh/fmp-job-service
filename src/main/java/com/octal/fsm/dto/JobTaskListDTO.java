package com.octal.fsm.dto;

import lombok.Data;

@Data
public class JobTaskListDTO {

    private String jobId;
    private String technicianId;
    private String technicianName;
    private String taskName;
    private String taskStatus;

    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
}
