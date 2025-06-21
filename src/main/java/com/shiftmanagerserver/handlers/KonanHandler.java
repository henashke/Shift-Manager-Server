package com.shiftmanagerserver.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.entities.Konan;
import com.shiftmanagerserver.service.KonanService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KonanHandler {
    private static final Logger logger = LoggerFactory.getLogger(KonanHandler.class);
    private final KonanService konanService;
    private final ObjectMapper objectMapper;

    public KonanHandler() {
        this.konanService = new KonanService();
        this.objectMapper = new ObjectMapper();
    }

    public void getAllKonanim(RoutingContext ctx) {
        logger.info("Received request to get all konanim");
        konanService.getAllKonans()
                .onSuccess(konanim -> {
                    try {
                        String json = objectMapper.writeValueAsString(konanim);
                        logger.info("Successfully retrieved {} konanim", konanim.size());
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing konanim", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.error("Failed to get konanim", err);
                    handleError(ctx, err);
                });
    }

    public void getKonanById(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        logger.info("Received request to get konan with id: {}", id);
        konanService.getKonanById(id)
                .onSuccess(konan -> {
                    try {
                        String json = objectMapper.writeValueAsString(konan);
                        logger.info("Successfully retrieved konan with id: {}", id);
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing konan", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.warn("Konan not found with id: {}", id);
                    ctx.response().setStatusCode(404).end();
                });
    }

    public void createKonan(RoutingContext ctx) {
        try {
            String body = ctx.body().asString();
            logger.info("Received request to create konan: {}", body);
            Konan konan = objectMapper.readValue(body, Konan.class);
            konanService.createKonan(konan)
                    .onSuccess(createdKonan -> {
                        try {
                            String json = objectMapper.writeValueAsString(createdKonan);
                            logger.info("Successfully created konan with id: {}", createdKonan.getId());
                            createHttpResponse(ctx, json, 201);
                        } catch (JsonProcessingException e) {
                            logger.error("Error serializing created konan", e);
                            handleError(ctx, e);
                        }
                    })
                    .onFailure(err -> {
                        logger.error("Failed to create konan", err);
                        handleError(ctx, err);
                    });
        } catch (JsonProcessingException e) {
            logger.error("Error parsing konan request body", e);
            handleError(ctx, e);
        }
    }

    public void updateKonan(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject body = ctx.body().asJsonObject();
        logger.info("Received request to update konan with id: {} and body: {}", id, body);
        konanService.updateKonan(id, body)
                .onSuccess(updatedKonan -> {
                    try {
                        String json = objectMapper.writeValueAsString(updatedKonan);
                        logger.info("Successfully updated konan with id: {}", id);
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing updated konan", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.warn("Konan not found for update with id: {}", id);
                    ctx.response().setStatusCode(404).end();
                });
    }

    public void deleteKonan(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        logger.info("Received request to delete konan with id: {}", id);
        konanService.deleteKonan(id)
                .onSuccess(v -> {
                    logger.info("Successfully deleted konan with id: {}", id);
                    ctx.response().setStatusCode(204).end();
                })
                .onFailure(err -> {
                    logger.warn("Konan not found for deletion with id: {}", id);
                    ctx.response().setStatusCode(404).end();
                });
    }

    private void createHttpResponse(RoutingContext ctx, String response, int statusCode) {
        ctx.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(statusCode)
                .end(response);
    }

    private void handleError(RoutingContext ctx, Throwable e) {
        logger.error("Error processing request", e);
        ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", e.getMessage()).encode());
    }
}
