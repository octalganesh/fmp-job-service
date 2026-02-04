package com.octal.fsm.entities.enums;

public enum QbdItemType {

    SERVICE("Service"),
    INVENTORY_PART("Inventory Part"),
    INVENTORY_ASSEMBLY("Inventory Assembly"),
    NON_INVENTORY_PART("Non-inventory Part"),
    FIXED_ASSET("Fixed Asset"),
    OTHER_CHARGE("Other Charge"),
    SUBTOTAL("Subtotal"),
    GROUP("Group"),
    DISCOUNT("Discount"),
    PAYMENT("Payment"),
    SALES_TAX_ITEM("Sales Tax Item"),
    SALES_TAX_GROUP("Sales Tax Group"),
    UNKNOWN("Unknown");

    private final String qbName;

    QbdItemType(String qbName) {
        this.qbName = qbName;
    }

    public String getQbName() {
        return qbName;
    }
}
