package com.octal.fsm.dto.enums;

public enum PushNotificationType {
    NEW_TASK_ASSIGNED("NEW_TASK_ASSIGNED");
    private final String status;

    PushNotificationType(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
