package com.octal.fsm.entities.enums;

public enum Category {

    BUSHES("BUSHES"), POOL("POOL"), PANELS("PANELS"),GATES("GATES"),BUILDING("BUILDING"),PLANTS("PLANTS"),FURNITURE("FURNITURE");
    private final String type;

    Category(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
