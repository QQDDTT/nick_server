package com.dream.nick_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import com.dream.nick_server.Admin.FilesConsole;
import com.dream.nick_server.handler.ChatHandler;
import com.dream.nick_server.handler.FileHandler;
import com.dream.nick_server.handler.HomeHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<?> routeCity(HomeHandler handler) {
        return RouterFunctions
                .route(isHttp()
                        .and(RequestPredicates.method(HttpMethod.GET))
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                        handler::get)
                .andRoute(isHttp()
                        .and(RequestPredicates.method(HttpMethod.POST))
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                        handler::post);
    }

    @Bean
    public HandlerMapping webSocketMapping(final ChatHandler chatHandler, final FileHandler fileHandler) {
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/echo", chatHandler);
        map.put("/" + FilesConsole.PATH_VAR, fileHandler);

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
