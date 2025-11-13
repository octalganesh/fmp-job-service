package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FormsResponseDTO {
    private String jobTypeId;
    private String jobId;
    private List<FormsManagementDTO.Detail> formDetails;
}
