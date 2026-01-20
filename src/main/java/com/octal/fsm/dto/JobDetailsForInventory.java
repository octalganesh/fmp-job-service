package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailsForInventory {
    private String jobId;
    private String jobDescription;
    private String jobType;
    private String taskShowId;
    private String taskUuid;
    private String taskName;
    private String taskStatus;
}
