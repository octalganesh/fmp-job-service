package com.octal.fsm.dto.enums;


public enum JobUpdateType {
    CANCEL,
    REASSIGN;
    public static boolean isEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        try {
            JobUpdateType.valueOf(value);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}