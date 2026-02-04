package com.octal.fsm.entities;

import com.octal.fsm.entities.enums.QbdItemType;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "inventory_part")
@Data
public class InventoryPart extends AbstractPersistable {

    @Column(name = "list_id", nullable = false, unique = true)
    private String listId;
    private String timeCreated;
    private String timeModified;
    private String editSequence;
    private String name;
    private String fullName;
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
    @Enumerated(EnumType.STRING)
    private QbdItemType itemType; //Custom Defined Column
}
