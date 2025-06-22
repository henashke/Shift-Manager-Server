package com.shiftmanagerserver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.shiftmanagerserver.handlers.AuthHandler;
import com.shiftmanagerserver.handlers.ConstraintHandler;
import com.shiftmanagerserver.handlers.ShiftHandler;
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
    private final ShiftHandler shiftHandler;

    @Inject
    public MainVerticle(@Named("application.port") Integer port, Router router,
                        UserHandler userHandler, AuthHandler authHandler, ConstraintHandler constraintHandler, ShiftHandler shiftHandler) {
        this.port = port;
        this.userHandler = userHandler;
        this.authHandler = authHandler;
        this.constraintHandler = constraintHandler;
        this.shiftHandler = shiftHandler;
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
        authHandler.addRoutes(router);
        userHandler.addRoutes(router);
        constraintHandler.addRoutes(router);
        shiftHandler.addRoutes(router);
    }

}
