package com.octal.fsm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EstimateRequestDto {

    @Data
    public static class Create {
        private String estimateId;
        private String jobId;
        private String taskId;
        private String status;
        private BigDecimal subTotal;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal grandTotal;
        private String documentUrl;
        private List<EstimateItemDto> items;

    }

    @Data
    public static class EstimateItemDto  {
        private String listId;
        private String itemName;
        private String itemType;
        private BigDecimal unitCost;
        private Integer quantity;
        private BigDecimal totalValue;
    }

}
