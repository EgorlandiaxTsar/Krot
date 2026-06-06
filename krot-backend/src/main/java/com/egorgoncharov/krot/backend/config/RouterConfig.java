package com.egorgoncharov.krot.backend.config;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class RouterConfig {
    void router(@Observes Router router) {
        router.route("/api/*").order(10).handler(BodyHandler.create());
    }
}
