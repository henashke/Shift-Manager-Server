package com.shiftmanagerserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.AssignedShift;
import com.shiftmanagerserver.entities.ShiftType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ShiftService {
    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);
    private static final String SHIFTS_FILE = "shifts.json";
    private final ObjectMapper objectMapper;
    private List<AssignedShift> shifts;

    public ShiftService() {
        this.objectMapper = new ObjectMapper();
        this.shifts = new ArrayList<>();
        loadShifts();
    }

    private void loadShifts() {
        File file = new File(SHIFTS_FILE);
        if (file.exists()) {
            try {
                shifts = objectMapper.readValue(file, new TypeReference<List<AssignedShift>>() {
                });
            } catch (IOException e) {
                logger.error("Error loading shifts from file", e);
                shifts = new ArrayList<>();
            }
        } else {
            shifts = new ArrayList<>();
        }
    }

    private void saveShifts() {
        try {
            objectMapper.writeValue(new File(SHIFTS_FILE), shifts);
        } catch (IOException e) {
            logger.error("Error saving shifts to file", e);
        }
    }

    public List<AssignedShift> getAllShifts() {
        return new ArrayList<>(shifts);
    }

    public void addShift(AssignedShift shift) {
        if (shift.uuid() == null) {
            shift.setUuid(UUID.randomUUID());
        }
        shifts.add(shift);
        saveShifts();
    }

    public void addShifts(List<AssignedShift> newShifts) {
        for (AssignedShift shift : newShifts) {
            if (shift.uuid() == null) {
                shift.setUuid(UUID.randomUUID());
            }
            shifts.add(shift);
        }
        saveShifts();
    }

    public boolean deleteShift(Date date, ShiftType type) {
        int initialSize = shifts.size();
        shifts.removeIf(s -> s.getType() == type && sameDay(s.getDate(), date));
        boolean removed = shifts.size() < initialSize;
        if (removed) saveShifts();
        return removed;
    }

    private boolean sameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        java.util.Calendar c1 = java.util.Calendar.getInstance();
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR)
                && c1.get(java.util.Calendar.MONTH) == c2.get(java.util.Calendar.MONTH)
                && c1.get(java.util.Calendar.DAY_OF_MONTH) == c2.get(java.util.Calendar.DAY_OF_MONTH);
    }
}
