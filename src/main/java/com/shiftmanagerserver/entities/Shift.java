package com.shiftmanagerserver.entities;

import java.time.LocalDate;
import java.util.UUID;

public class Shift {
    private UUID uuid;

    public LocalDate date() {
        return date;
    }

    public boolean isDay() {
        return isDay;
    }

    private LocalDate date;
    private boolean isDay;

    public Shift(UUID uuid, LocalDate date, boolean isDay) {
        this.uuid = uuid;
        this.date = date;
        this.isDay = isDay;
    }

    public UUID uuid() {
        return uuid;
    }


}
