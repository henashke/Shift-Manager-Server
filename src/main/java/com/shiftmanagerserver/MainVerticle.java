package com.shiftmanagerserver;

import com.shiftmanagerserver.handler.AuthHandler;
import com.shiftmanagerserver.handler.KonanHandler;
import com.shiftmanagerserver.service.KonanService;
import com.shiftmanagerserver.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private final KonanService konanService;
    private final UserService userService;
    private final KonanHandler konanHandler;
    private final AuthHandler authHandler;

    public MainVerticle() {
        this.konanService = new KonanService();
        this.userService = new UserService();
        this.konanHandler = new KonanHandler(konanService);
        this.authHandler = new AuthHandler(userService);
    }

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

        // Auth routes
        router.post("/auth/signup").handler(authHandler::handleSignup);
        router.post("/auth/login").handler(authHandler::handleLogin);

        // Konan routes
        router.get("/konanim").handler(konanHandler::getAllKonanim);
        router.get("/konanim/:id").handler(konanHandler::getKonanById);
        router.post("/konanim").handler(konanHandler::createKonan);
        router.put("/konanim/:id").handler(konanHandler::updateKonan);
        router.delete("/konanim/:id").handler(konanHandler::deleteKonan);

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
}
