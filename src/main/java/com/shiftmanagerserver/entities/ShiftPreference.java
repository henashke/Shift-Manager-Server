package com.shiftmanagerserver.entities;

import java.util.UUID;

public class ShiftPreference {

    public Shift shift() {
        return shift;
    }

    private final Shift shift;

    private final String preference;

    public ShiftPreference(Shift shift, String preference) {
        this.shift = shift;
        this.preference = preference;
    }

    public String preference() {
        return preference;
    }

    public UUID uuid() {
        return shift.uuid();
    }
}
