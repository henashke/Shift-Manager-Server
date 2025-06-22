package com.shiftmanagerserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.AssignedShift;
import com.shiftmanagerserver.entities.Shift;
import com.shiftmanagerserver.entities.ShiftType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
                shifts = objectMapper.readValue(file, new TypeReference<>() {
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

    public void addShifts(List<AssignedShift> newShifts) {
        for (AssignedShift shift : newShifts) {
            shifts.removeIf(s -> Objects.equals(s, shift));
            shift.setUuid(UUID.randomUUID());
            shifts.add(shift);
        }
        saveShifts();
    }

    public boolean deleteShift(Date date, ShiftType type) {
        int initialSize = shifts.size();
        shifts.removeIf(s -> Objects.equals(s, new Shift(date, type)));
        boolean removed = shifts.size() < initialSize;
        if (removed) saveShifts();
        return removed;
    }

}
