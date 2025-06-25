package com.shiftmanagerserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.*;
import com.shiftmanagerserver.service.ShiftService;
import com.shiftmanagerserver.service.ShiftWeightSettingsService;
import com.shiftmanagerserver.service.UserService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ShiftHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(ShiftHandler.class);
    private final ShiftService shiftService;

    private final UserService userService;

    private final ShiftWeightSettingsService shiftWeightSettingsService;
    private final ObjectMapper objectMapper;

    public ShiftHandler() {
        this.shiftService = new ShiftService();
        this.objectMapper = new ObjectMapper();
        this.userService = new UserService();
        this.shiftWeightSettingsService = new ShiftWeightSettingsService();
    }

    public void getAllShifts(RoutingContext ctx) {
        try {
            List<AssignedShift> shifts = shiftService.getAllShifts();
            JsonArray arr = new JsonArray(objectMapper.writeValueAsString(shifts));
            ctx.response().putHeader("Content-Type", "application/json").end(arr.encode());
        } catch (Exception e) {
            logger.error("Error fetching all shifts", e);
            ctx.response().setStatusCode(500).end();
        }
    }

    public void addShifts(RoutingContext ctx) {
        try {
            List<AssignedShift> shifts = objectMapper.readValue(ctx.body().asString(), objectMapper.getTypeFactory().constructCollectionType(List.class, AssignedShift.class));
            shiftService.addShifts(shifts);
            ctx.response().setStatusCode(201).end();
        } catch (Exception e) {
            logger.error("Error adding multiple shifts", e);
            ctx.response().setStatusCode(400).end();
        }
    }

    public void deleteShift(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();
            String dateStr = body.getString("date");
            String typeStr = body.getString("type");
            if (dateStr == null || typeStr == null) {
                ctx.response().setStatusCode(400).end();
                return;
            }
            Date date = objectMapper.getDateFormat().parse(dateStr);
            ShiftType type = ShiftType.fromHebrewName(typeStr);
            boolean deleted = shiftService.deleteShift(date, type);
            if (deleted) {
                ctx.response().setStatusCode(200).end();
            } else {
                ctx.response().setStatusCode(404).end();
            }
        } catch (Exception e) {
            logger.error("Error deleting shift", e);
            ctx.response().setStatusCode(400).end();
        }
    }

    public void setShifts(RoutingContext ctx) {
        try {
            ShiftWeightSettings settings = shiftWeightSettingsService.getSettings();
            String currentPreset = settings.getCurrentPreset();
            List<ShiftWeight> shiftsWeight= settings.getPresets().get(currentPreset).getWeights();
            List<User> users = userService.getAllUsers();

            shiftsWeight.sort((a, b) -> Integer.compare(b.getWeight(), a.getWeight()));
            users.sort(Comparator.comparingInt(User::getScore));

            List<AssignedShift> assignedShifts = new ArrayList<>();
            int userIndex = 0;

            for (ShiftWeight shiftWeight : shiftsWeight) {
                // get back to first user when assigned a shift for all users
                if (userIndex >= users.size()) {
                    users.sort(Comparator.comparingInt(User::getScore)); // resort after scores changed
                    userIndex = 0;
                }

                User user = users.get(userIndex);
                userIndex++;

                // Assign the shift
                Shift shift = new Shift(generateDateFromDay(shiftWeight.getDay()), shiftWeight.getShiftType());
                AssignedShift assignedShift = new AssignedShift(user.getId(), shift);
                assignedShifts.add(assignedShift);

                // Update user's score
                user.setScore(user.getScore() + shiftWeight.getWeight());
            }

            // Save assigned shifts
            shiftService.addShifts(assignedShifts);

            //updated user scores
            for (User user : users) {
                JsonObject updates = new JsonObject().put("score", user.getScore());
                userService.updateUser(user.getId(), updates);
            }

            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(200).end();

        } catch (Exception e) {
            e.printStackTrace();
            ctx.response().setStatusCode(500).end("Error assigning shifts");
        }
    }

    private Date generateDateFromDay(Day day) {
        LocalDate now = LocalDate.now();
        DayOfWeek target = DayOfWeek.valueOf(day.name());
        int daysToAdd = (target.getValue() - now.getDayOfWeek().getValue() + 7) % 7;
        LocalDate nextDate = now.plusDays(daysToAdd);
        return Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void addRoutes(io.vertx.ext.web.Router router) {
        router.get("/shifts").handler(this::getAllShifts);
        router.post("/shifts").handler(this::addShifts);
        router.delete("/shifts").handler(this::deleteShift);
        router.post("/set-shifts").handler(this::setShifts);
    }
}
