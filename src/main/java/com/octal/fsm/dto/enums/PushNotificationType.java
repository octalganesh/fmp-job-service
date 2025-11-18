package com.octal.fsm.dto.enums;

public enum PushNotificationType {
    NEW_TASK_ASSIGNED("NEW_TASK_ASSIGNED"),
    TASK_STATUS_CHANGE("TASK_STATUS_CHANGE");
    private final String status;

    PushNotificationType(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
