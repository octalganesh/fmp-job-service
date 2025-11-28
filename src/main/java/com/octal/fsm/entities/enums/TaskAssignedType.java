package com.octal.fsm.entities.enums;

public enum TaskAssignedType {
    TECHNICIAN("TECHNICIAN"), CSR("CSR"), SYSTEM("SYSTEM");
    private final String type;

    TaskAssignedType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
