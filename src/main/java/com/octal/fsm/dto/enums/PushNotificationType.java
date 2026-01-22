package com.octal.fsm.dto.enums;

public enum PushNotificationType {
    NEW_TASK_ASSIGNED("NEW_TASK_ASSIGNED"),
    TASK_STATUS_CHANGE("TASK_STATUS_CHANGE"),
    INVENTORY_REQUEST_CANCELLED("INVENTORY_REQUEST_CANCELLED"),
    NEW_APPOINTMENT_ASSIGNED("NEW_APPOINTMENT_ASSIGNED"),
    INVENTORY_REQUEST_APPROVED("INVENTORY_REQUEST_APPROVED");
    private final String status;

    PushNotificationType(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
