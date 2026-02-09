package com.octal.fsm.dto;

import lombok.Data;

@Data
public class EstimateDocumentRequestDto {
    private String estimateId;
    private String documentUrl;
    private String documentType;
}
