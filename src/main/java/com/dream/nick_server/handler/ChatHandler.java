package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.dream.nick_server.chat.ChatServer;

import reactor.core.publisher.Mono;

@Component
public class ChatHandler implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHandler.class);

    @Autowired
    private ChatServer chatServer;

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[CONNECT] WebSocket");

        return session.send(
                session.receive()
                        .doOnNext(msg -> {
                            String message = msg.getPayloadAsText();
                            LOGGER.info("[RECEIVED]: {}", message);
                        })
                        .map(msg -> {
                            String message = msg.getPayloadAsText();
                            String response = chatServer.answer(message);
                            LOGGER.info("[RESPONSE] : {}", response);
                            return session.textMessage(response);
                        })
        ).doOnTerminate(() -> LOGGER.info("[DISCONNECT] WebSocket"));
    }
}
