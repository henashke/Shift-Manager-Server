package com.shiftmanagerserver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.shiftmanagerserver.handlers.AuthHandler;
import com.shiftmanagerserver.handlers.ConstraintHandler;
import com.shiftmanagerserver.handlers.KonanHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private final Logger logger;
    private final Integer port;
    private final KonanHandler konanHandler;
    private final Router router;
    private final ConstraintHandler constraintHandler;
    private final AuthHandler authHandler;

    @Inject
    public MainVerticle(@Named("application.port") Integer port, Router router,
                        KonanHandler konanHandler, AuthHandler authHandler, ConstraintHandler constraintHandler) {
        this.port = port;
        this.konanHandler = konanHandler;
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

        router.get("/konanim").handler(konanHandler::getAllKonanim);
        router.get("/konanim/:id").handler(konanHandler::getKonanById);
        router.post("/konanim").handler(konanHandler::createKonan);
        router.put("/konanim/:id").handler(konanHandler::updateKonan);
        router.delete("/konanim/:id").handler(konanHandler::deleteKonan);

        router.post("/constraints").handler(constraintHandler::handleCreateConstraint);
        router.get("/constraints").handler(constraintHandler::handleGetAllConstraints);
        router.get("/constraints/konan/:konanId").handler(constraintHandler::handleGetConstraintsByKonanId);
        router.delete("/constraints").handler(constraintHandler::handleDeleteConstraint);
    }

} 