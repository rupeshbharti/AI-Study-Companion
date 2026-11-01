package com.studyaid.entity;

public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM;

    public static MessageRole fromString(String val) {
        if (val == null) return USER;
        try {
            return MessageRole.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}

