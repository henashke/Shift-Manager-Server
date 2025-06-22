package com.shiftmanagerserver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.shiftmanagerserver.handlers.AuthHandler;
import com.shiftmanagerserver.handlers.ConstraintHandler;
import com.shiftmanagerserver.handlers.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private final Logger logger;
    private final Integer port;
    private final UserHandler userHandler;
    private final Router router;
    private final ConstraintHandler constraintHandler;
    private final AuthHandler authHandler;

    @Inject
    public MainVerticle(@Named("application.port") Integer port, Router router,
                        UserHandler userHandler, AuthHandler authHandler, ConstraintHandler constraintHandler) {
        this.port = port;
        this.userHandler = userHandler;
        this.authHandler = authHandler;
        this.constraintHandler = constraintHandler;
        this.router = router;
        this.logger = LoggerFactory.getLogger(MainVerticle.class);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("Starting Shift-Manager application...");
        bindRoutes(router);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        logger.info("HTTP server started on port " + port);
                    } else {
                        logger.error("Failed to start HTTP server", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void bindRoutes(Router router) {

        router.post("/auth/signup").handler(authHandler::handleSignup);
        router.post("/auth/login").handler(authHandler::handleLogin);

        // User routes
        router.get("/users").handler(userHandler::getAllUsers);
        router.get("/users/:id").handler(userHandler::getUserById);
        router.post("/users").handler(userHandler::createUser);
        router.put("/users/:id").handler(userHandler::updateUser);
        router.delete("/users/:id").handler(userHandler::deleteUser);

        router.post("/constraints").handler(constraintHandler::handleCreateConstraint);
        router.get("/constraints").handler(constraintHandler::handleGetAllConstraints);
        router.get("/constraints/user/:userId").handler(constraintHandler::handleGetConstraintsByUserId);
        router.delete("/constraints").handler(constraintHandler::handleDeleteConstraint);
    }

}
