package com.octal.fsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JobTaskMappingWithHTMLFormDTO {

    private String uuid;
    private String jobTaskMappingId;
    private String technicianId;
    private String taskStatus;
    private String note;
    private String technicianNote;
    private String startDate;
    private String endDate;
    private String signature;
    private String cancelReason;
    private String drawingJson;
    private String drawingImage;
    private List<HTMLFormDTO.Details> htmlForms;
}
