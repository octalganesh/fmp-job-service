package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryRequestDTO {

    @Data
    public static class Create {
        private String technicianId;
        private String taskId;
        private List<Item> items;
    }

    @Data
    public static class Item {
        private String inventoryListId;
        private String inventoryName;
        private Integer quantity;
    }

}
