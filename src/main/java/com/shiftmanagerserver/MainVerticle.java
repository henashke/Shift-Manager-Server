package main.java.com.shiftmanagerserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import main.java.com.shiftmanagerserver.model.Konan;
import main.java.com.shiftmanagerserver.service.KonanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private final KonanService konanService = new KonanService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("Starting Shift-Manager application...");
        Router router = Router.router(vertx);

        // Enable CORS
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedHeader("Content-Type"));

        // Parse request body
        router.route().handler(BodyHandler.create());

        // Routes
        router.get("/konanim").handler(this::getAllKonanim);
        router.get("/konanim/:id").handler(this::getKonanById);
        router.post("/konanim").handler(this::createKonan);
        router.put("/konanim/:id").handler(this::updateKonan);
        router.delete("/konanim/:id").handler(this::deleteKonan);

        // Create HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        logger.info("HTTP server started on port 8080");
                    } else {
                        logger.error("Failed to start HTTP server", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void getAllKonanim(RoutingContext ctx) {
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

    private void getKonanById(RoutingContext ctx) {
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

    private void createKonan(RoutingContext ctx) {
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

    private void updateKonan(RoutingContext ctx) {
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

    private void deleteKonan(RoutingContext ctx) {
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
            .end("{\"error\":\"" + e.getMessage() + "\"}");
    }
} 