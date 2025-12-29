package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryApprovalDTO {

    private String approvedBy;
    private String action; // ACCEPT or CANCEL
    private String rejectionReason;
    private String id;
    private List<ItemApproval> items;

    @Data
    public static class ItemApproval {
        private String inventoryListId;
        private Integer approvedQty; // 0 or null → rejected
    }
}
