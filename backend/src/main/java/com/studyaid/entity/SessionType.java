package com.studyaid.entity;

public enum SessionType {
    GENERAL,
    TUTORIAL,
    ASSIGNMENT,
    HOMEWORK;

    public static SessionType fromString(String val) {
        if (val == null) return GENERAL;
        try {
            return SessionType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}

