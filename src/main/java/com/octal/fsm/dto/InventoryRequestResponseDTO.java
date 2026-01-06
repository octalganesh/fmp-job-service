package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryRequestResponseDTO {

    private String id;
    private String technicianId;
    private String taskId;
    private String status;
    private String comment;

    private String requestedAt;
    private String approvedAt;
    private String approvedBy;
    private String rejectionReason;

    private List<Item> items;

    @Data
    public static class Item {
        private String inventoryListId;
        private Integer requestedQty;
        private Integer approvedQty;
        private String inventoryName;
    }
}
