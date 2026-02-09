package com.octal.fsm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class EstimateResponseDto {
    @Data
    public static class list {
        private String estimateId;
        private String jobId;
        private String taskId;
        private String status;
        private BigDecimal subTotal;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal grandTotal;
        private boolean activeStatus;
        private String tenantId;
        private String createdAt;
        private List<EstimateRequestDto.EstimateItemDto> items;
        private List<EstimateDocumentResponseDto> estimateBills;

    }
}
