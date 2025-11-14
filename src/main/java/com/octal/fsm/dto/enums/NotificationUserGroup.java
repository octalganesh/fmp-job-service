package com.octal.fsm.dto.enums;

public enum NotificationUserGroup {

    ALL_USER("ALL_USER"),
    PARTICULAR_USER("PARTICULAR_USER"),
    ALL_ANDROID_USER("ALL_ANDROID_USER"),
    ALL_IOS_USER("ALL_IOS_USER");

    private final String type;

    NotificationUserGroup(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public String getType() {
        return type;
    }
}
