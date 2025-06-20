package com.shiftmanagerserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.shiftmanagerserver.model.User;
import com.shiftmanagerserver.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private final Logger logger;
    private final UserService userService;
    private final ObjectMapper mapper;
    private final Router router;
    private final Integer port;

    @Inject
    public MainVerticle(@Named("application.port") Integer port, Router router,
                        ObjectMapper mapper, UserService userService) {
        this.port = port;
        this.logger = LoggerFactory.getLogger(MainVerticle.class);
        this.mapper = mapper;
        this.userService = userService;
        this.router = router;
        bindRoutes();
    }

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("Starting Shift-Manager application...");
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        logger.info("HTTP server started on port 8080");
                    } else {
                        logger.error("Failed to start HTTP server", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void bindRoutes() {
        router.get("/users").handler(this::getAllUsers);
        router.get("/users/:id").handler(this::getUserById);
        router.post("/adduser").handler(this::createUser);
        router.put("/updateUser/:id").handler(this::updateUser);
        router.delete("/removeUser/:id").handler(this::deleteUser);
    }

    private void getAllUsers(RoutingContext ctx) {
        logger.info("Received request to get all userim");
        userService.users()
                .onSuccess(userim -> {
                    try {
                        String json = mapper.writeValueAsString(userim);
                        logger.info("Successfully retrieved {} userim", userim.size());
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing userim", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.error("Failed to get userim", err);
                    handleError(ctx, err);
                });
    }

    private void getUserById(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        logger.info("Received request to get user with id: {}", id);
        userService.getUserById(id)
                .onSuccess(user -> {
                    try {
                        String json = mapper.writeValueAsString(user);
                        logger.info("Successfully retrieved user with id: {}", id);
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing user", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.warn("User not found with id: {}", id);
                    ctx.response().setStatusCode(404).end();
                });
    }

    private void createUser(RoutingContext ctx) {
        try {
            String body = ctx.body().asString();
            logger.info("Received request to create user: {}", body);
            User user = mapper.readValue(body, User.class);
            userService.createUser(user)
                    .onSuccess(createdUser -> {
                        try {
                            String json = mapper.writeValueAsString(createdUser);
                            logger.info("Successfully created user with id: {}", createdUser.getId());
                            createHttpResponse(ctx, json, 201);
                        } catch (JsonProcessingException e) {
                            logger.error("Error serializing created user", e);
                            handleError(ctx, e);
                        }
                    })
                    .onFailure(err -> {
                        logger.error("Failed to create user", err);
                        handleError(ctx, err);
                    });
        } catch (JsonProcessingException e) {
            logger.error("Error parsing user request body", e);
            handleError(ctx, e);
        }
    }

    private void updateUser(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject body = ctx.body().asJsonObject();
        logger.info("Received request to update user with id: {} and body: {}", id, body);
        userService.updateUser(id, body)
                .onSuccess(updatedUser -> {
                    try {
                        String json = mapper.writeValueAsString(updatedUser);
                        logger.info("Successfully updated user with id: {}", id);
                        createHttpResponse(ctx, json, 200);
                    } catch (JsonProcessingException e) {
                        logger.error("Error serializing updated user", e);
                        handleError(ctx, e);
                    }
                })
                .onFailure(err -> {
                    logger.warn("User not found for update with id: {}", id);
                    ctx.response().setStatusCode(404).end();
                });
    }

    private void deleteUser(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        logger.info("Received request to delete user with id: {}", id);
        userService.deleteUser(id)
                .onSuccess(v -> {
                    logger.info("Successfully deleted user with id: {}", id);
                    ctx.response().setStatusCode(204).end();
                })
                .onFailure(err -> {
                    logger.warn("User not found for deletion with id: {}", id);
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