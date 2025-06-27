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

    public void suggestShiftAssignment(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();
            List<String> userIds = body.getJsonArray("userIds").getList();
            String startDateStr = body.getString("startDate");
            String endDateStr = body.getString("endDate");
            if (userIds == null || startDateStr == null || endDateStr == null) {
                ctx.response().setStatusCode(400).end();
                return;
            }
            Date startDate = objectMapper.getDateFormat().parse(startDateStr);
            Date endDate = objectMapper.getDateFormat().parse(endDateStr);
            List<Shift> relevantShifts = generateShiftsBetween(startDate, endDate);

            UserService userService = new UserService();
            ConstraintService constraintService = new ConstraintService();
            Map<User, List<Constraint>> userConstraintMap = new HashMap<>();
            for (String userId : userIds) {
                User user = userService.getUserById(userId);
                List<Constraint> constraints = constraintService.getConstraintsByUserId(userId);
                userConstraintMap.put(user, constraints);
            }
            List<AssignedShift> suggestedShifts = shiftService.suggestShiftAssignment(relevantShifts, userConstraintMap);
            ctx.response().setStatusCode(200).end();
        } catch (Exception e) {
            logger.error("Error in suggestShiftAssignment", e);
            ctx.response().setStatusCode(400).end();
        }
    }

    public List<Shift> generateShiftsBetween(Date startDate, Date endDate) {
        List<Shift> shifts = new ArrayList<>();

        Calendar current = Calendar.getInstance();
        current.setTime(startDate);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        while (!current.after(end)) {
            Date shiftDate = current.getTime();

            // Generate DAY and NIGHT shifts for this date
            shifts.add(new Shift(shiftDate, ShiftType.DAY));
            shifts.add(new Shift(shiftDate, ShiftType.NIGHT));

            // Move to next day
            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        return shifts;
    }

    @Override
    public void addRoutes(io.vertx.ext.web.Router router) {
        router.get("/shifts").handler(this::getAllShifts);
        router.post("/shifts").handler(this::addShifts);
        router.delete("/shifts").handler(this::deleteShift);
        router.post("/shifts/suggest-assignment").handler(this::suggestShiftAssignment);
    }
}
