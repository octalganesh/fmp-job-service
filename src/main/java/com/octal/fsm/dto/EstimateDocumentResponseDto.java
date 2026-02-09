package com.octal.fsm.dto;

import lombok.Data;

@Data
public class EstimateDocumentResponseDto {

    private Long id;
    private String documentUrl;
    private String documentType;
    private String estimateId;
}
