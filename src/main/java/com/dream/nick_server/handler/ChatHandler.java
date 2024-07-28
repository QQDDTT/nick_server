package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

@Component
public class ChatHandler implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHandler.class);
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[CHAT SOCKET]:" + session.getId());
        return session.send(
                session.receive()
                        .map(msg -> session.textMessage(
                                "服务端返回: " + msg.getPayloadAsText())));
    }
}