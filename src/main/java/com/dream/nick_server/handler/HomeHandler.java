package com.dream.nick_server.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class HomeHandler {
    public Mono<ServerResponse> index(ServerRequest request) {
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render("index");
    }

    public Mono<ServerResponse> home(ServerRequest request) {
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render("home");
    }

    public Mono<ServerResponse> chat(ServerRequest request) {
        return ServerResponse
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .render("chat");
    }
}
