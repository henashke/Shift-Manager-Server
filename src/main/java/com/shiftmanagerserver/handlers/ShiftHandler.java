package com.shiftmanagerserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.Shift;
import com.shiftmanagerserver.entities.ShiftType;
import com.shiftmanagerserver.service.ShiftService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class ShiftHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(ShiftHandler.class);
    private final ShiftService shiftService;
    private final ObjectMapper objectMapper;

    public ShiftHandler() {
        this.shiftService = new ShiftService();
        this.objectMapper = new ObjectMapper();
    }

    public void getAllShifts(RoutingContext ctx) {
        try {
            List<Shift> shifts = shiftService.getAllShifts();
            JsonArray arr = new JsonArray(objectMapper.writeValueAsString(shifts));
            ctx.response().putHeader("Content-Type", "application/json").end(arr.encode());
        } catch (Exception e) {
            logger.error("Error fetching all shifts", e);
            ctx.response().setStatusCode(500).end();
        }
    }

    public void addShift(RoutingContext ctx) {
        try {
            Shift shift = objectMapper.readValue(ctx.body().asString(), Shift.class);
            shiftService.addShift(shift);
            ctx.response().setStatusCode(201).end();
        } catch (Exception e) {
            logger.error("Error adding shift", e);
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

    @Override
    public void addRoutes(io.vertx.ext.web.Router router) {
        router.get("/shifts").handler(this::getAllShifts);
        router.post("/shifts").handler(this::addShift);
        router.delete("/shifts").handler(this::deleteShift);
    }
}
