package com.shiftmanagerserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.*;
import com.shiftmanagerserver.service.ConstraintService;
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
    private final ConstraintService constraintService;
    private final ShiftWeightSettingsService shiftWeightSettingsService;
    private final ObjectMapper objectMapper;

    public ShiftHandler() {
        this.shiftService = new ShiftService();
        this.objectMapper = new ObjectMapper();
        this.userService = new UserService();
        this.shiftWeightSettingsService = new ShiftWeightSettingsService();
        this.constraintService = new ConstraintService();
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
            String body = ctx.body().asString();

            List<User> users = objectMapper.readValue(body, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
            ShiftWeightSettings settings = objectMapper.readValue(body, ShiftWeightSettings.class);
            List<Constraint> constraints = objectMapper.readValue(body, objectMapper.getTypeFactory().constructCollectionType(List.class, Constraint.class));

            constraintService.addConstraints(constraints);
            String currentPreset = settings.getCurrentPreset();
            List<ShiftWeight> shiftsWeight = settings.getPresets().get(currentPreset).getWeights();

            shiftsWeight.sort((a, b) -> Integer.compare(b.getWeight(), a.getWeight()));
            users.sort(Comparator.comparingInt(User::getScore));

            List<AssignedShift> assignedShifts = new ArrayList<>();
            Map<String, List<Shift>> userShifts = new HashMap<>();
            Map<String, Integer> userMissedDays = new HashMap<>();

            for (User user : users) {
                userShifts.put(user.getId(), new ArrayList<>());
                userMissedDays.put(user.getId(), 0);
            }

            for (ShiftWeight shiftWeight : shiftsWeight) {
                Shift newShift = new Shift(generateDateFromDay(shiftWeight.getDay()), shiftWeight.getShiftType());

                boolean assigned = false;

                users.sort(Comparator.comparingInt(User::getScore));

                for (User user : users) {
                    String userId = user.getId();
                    List<Shift> pastShifts = userShifts.get(userId);
                    int currentMissed = userMissedDays.get(userId);

                    boolean blocked = constraints.stream().anyMatch(c ->
                            c.getUserId().equals(userId) &&
                                    c.getShift().getType() == newShift.getType() &&
                                    isSameDay(c.getShift().getDate(), newShift.getDate()) &&
                                    c.getConstraintType() == ConstraintType.CANT
                    );

                    if (blocked)
                        continue;

                    // Enforce 48 gap between shifts
                    boolean has48hGap = pastShifts.stream().allMatch(s ->
                            Math.abs(s.getDate().getTime() - newShift.getDate().getTime()) >= 48L * 60 * 60 * 1000
                    );

                    if (!has48hGap)
                        continue;

                    // Enforce max 2 missed office days
                    int missedDaysForShift = 0;
                    if (ShiftWeightPresetType.IMMEDIATE.name().equals(currentPreset)) {
                        missedDaysForShift = calculateMissedDays(shiftWeight.getDay(), shiftWeight.getShiftType());
                        if ((currentMissed + missedDaysForShift) > 2)
                            continue;
                    }

                    AssignedShift assignedShift = new AssignedShift(userId, newShift);
                    assignedShifts.add(assignedShift);
                    user.setScore(user.getScore() + shiftWeight.getWeight());

                    pastShifts.add(newShift);
                    userMissedDays.put(userId, currentMissed + missedDaysForShift);

                    assigned = true;
                    break;
                }

                if (!assigned) {
                    logger.warn("No suitable user found for shift: " + newShift.getDate() + " " + newShift.getType());
                }
            }

            shiftService.addShifts(assignedShifts);

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

    private int calculateMissedDays(Day day, ShiftType type) {
        switch (day) {
            case SUNDAY:
            case TUESDAY:
                return 1;
            case MONDAY:
            case WEDNESDAY:
                if (type == ShiftType.NIGHT) return 2;
                return 1;
            default:
                return 0;
        }
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
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
