package com.shiftmanagerserver.dto;


public enum Preference {
    CANNOT(Integer.MIN_VALUE),
    PREFER_NOT(-1),
    NO_PREFERENCE(0),
    PREFER(1),
    MUST(Integer.MAX_VALUE);

    private final int priority;

    Preference(int priority) {
        this.priority = priority;
    }
}