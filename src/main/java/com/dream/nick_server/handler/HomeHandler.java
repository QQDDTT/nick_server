package com.dream.nick_server.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class HomeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeHandler.class);
    
    public Mono<ServerResponse> index(ServerRequest request) {
        LOGGER.info("Index");
        Map<String, Object> model = new HashMap<>();
        model.put("message", "Welcome to FreeMarker with Spring WebFlux!");
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render("index",model);
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        String path = request.path();
        LOGGER.info("[GET]: " + path);
        request.queryParams().forEach((key, value) -> LOGGER.info("Query Param: " + key + " = " + value));
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render(path)
            .onErrorResume(e -> {
                LOGGER.error("Error rendering path: " + path, e);
                return ServerResponse.notFound().build();
            });
    }

    public Mono<ServerResponse> post(ServerRequest request) {
        String path = request.path();
        LOGGER.info("[POST]: " + path);
        request.formData().subscribe(data -> data.forEach((key, value) -> LOGGER.info("Form Data: " + key + " = " + value)));
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render(path)
            .onErrorResume(e -> {
                LOGGER.error("Error rendering path: " + path, e);
                return ServerResponse.notFound().build();
            });
    }
}
