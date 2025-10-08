package com.octal.fsm.entities.enums;

public enum Gender {
    MALE("MALE"), FEMALE("FEMALE"),OTHER("OTHER");
    private final String type;

    Gender(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}