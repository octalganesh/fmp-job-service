package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class DispatchBoardDataResponseDTO {

    private String customerId;
    private String customerName;
    private String technicianId;
    private String StartDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String taskMappingId;
    private String jobId;
    private List<JobTagDTO.Detail> jobTag;
    private String serviceLocation;
    private String jobTaskMappingTechnicianId;
    private String technicianName;
    private TechnicianDTO.GetDetails techDetails;
    private String jobTypeId;
    private String jobStatus;
    private String jobTypeName;

}
