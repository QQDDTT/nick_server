package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class SuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessHandler.class);

    public Mono<ServerResponse> get(ServerRequest request) {
        if(request.path().equals("/")){
            return getHTML("index");
        }else{
            return getHTML(request.path());
        }
    }

    public Mono<ServerResponse> post(ServerRequest request) {
        return getHTML(request.path());
    }

    public Mono<ServerResponse> favicon(ServerRequest request){
        Resource resource = new ClassPathResource("static/favicon.ico");
        return ServerResponse
            .ok()
            .contentType(MediaType.valueOf("image/x-icon"))
            .bodyValue(resource)
            .onErrorResume(e -> {
                LOGGER.error("Error serving favicon: " , e);
                return ServerResponse.notFound().build();
            });
    }

    public Mono<ServerResponse> getStatic(ServerRequest request){
        String path = request.path();
        Resource resource = new ClassPathResource(path);
        return ServerResponse
            .ok()
            .contentType(MediaType.IMAGE_PNG)
            .bodyValue(resource)
            .onErrorResume(e -> {
                LOGGER.error("Error rendering path: " + path, e);
                return ServerResponse.notFound().build();
            });
    }
 
    private Mono<ServerResponse> getHTML(String path){
        LOGGER.info("[GET] " + path);
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
