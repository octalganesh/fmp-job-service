package com.octal.fsm.dto;

import lombok.Data;

@Data
public class InventoryPartDTO {

    @Data
    public static class Add{
        private String listId;
        private String timeCreated;
        private String timeModified;
        private String editSequence;
        private String name;
        private String fullName;
        private String isActive;
        private String sublevel;
        private String salesTaxCodeListId;
        private String salesTaxCodeFullName;
        private String salesOrPurchasePrice;
        private String salesOrPurchaseAccountFullName;
        private String salesOrPurchaseAccountListId;
        private String discountRatePercentage;
        private String accountListId;
        private String accountFullName;
        private String incomeAccountListId;
        private String incomeAccountFullName;
        private String cogsAccountListId;
        private String cogsAccountFullName;
        private String prefVendorListId;
        private String prefVendorFullName;
        private String assetAccountListId;
        private String assetAccountFullName;
        private String salesDesc;
        private String salesPrice;
        private String purchaseDesc;
        private String purchaseCost;
        private String reorderPoint;
        private String quantityOnHand;
        private String averageCost;
        private String quantityOnOrder;
        private String quantityOnSalesOrder;
        private String itemDesc;
        private String taxRate;
        private String taxVendorListId;
        private String taxVendorFullName;
        private String salePrice;
        private String itemType;
        private String createdAt;
    }

    @Data
    public static class Response{
        private String id;
        private String listId;
        private String timeCreated;
        private String timeModified;
        private String editSequence;
        private String name;
        private String fullName;
        private boolean isActive;
        private String sublevel;
        private String salesTaxCodeListId;
        private String salesTaxCodeFullName;
        private String salesOrPurchasePrice;
        private String salesOrPurchaseAccountFullName;
        private String salesOrPurchaseAccountListId;
        private String discountRatePercentage;
        private String accountListId;
        private String accountFullName;
        private String incomeAccountListId;
        private String incomeAccountFullName;
        private String cogsAccountListId;
        private String cogsAccountFullName;
        private String prefVendorListId;
        private String prefVendorFullName;
        private String assetAccountListId;
        private String assetAccountFullName;
        private String salesDesc;
        private String salesPrice;
        private String purchaseDesc;
        private String purchaseCost;
        private String reorderPoint;
        private String quantityOnHand;
        private String averageCost;
        private String quantityOnOrder;
        private String quantityOnSalesOrder;
        private String itemDesc;
        private String taxRate;
        private String taxVendorListId;
        private String taxVendorFullName;
        private String salePrice;
        private String itemType;
        private String createdAt;
    }
}
