package com.octal.fsm.entities.enums;

public enum  SystemEventType {
    INVOICE_CREATED("INVOICE_CREATED"),
    JOB_TAG_ADDED("JOB_TAG_ADDED"),
    TECHNICIAN_ASSIGNED_TO_TASK("TECHNICIAN_ASSIGNED_TO_TASK"),
    JOB_CREATED("JOB_CREATED"),
    JOB_STATUS_UPDATED("JOB_STATUS_UPDATED"),
    DOCUMENT_UPLOADED("DOCUMENT_UPLOADED"),
    APPOINTMENT_SCHEDULED("APPOINTMENT_SCHEDULED"),
    NOTE_ADDED("NOTE_ADDED"),
    EMAIL_SENT("EMAIL_SENT"),
    JOB_CALL_LOG_CREATED("JOB_CALL_LOG_CREATED");

    private final String type;

    SystemEventType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
