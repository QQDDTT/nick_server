package com.dream.nick_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dream.nick_server.handler.HomeHandler;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> routeCity(HomeHandler handler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/")
                        .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        handler::index)
                .andRoute(RequestPredicates.GET("/home")
                        .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        handler::home)
                .andRoute(RequestPredicates.GET("/chat")
                        .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        handler::chat)
                ;
    }
}
