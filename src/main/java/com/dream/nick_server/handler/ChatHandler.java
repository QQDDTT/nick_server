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
    private ChatServer cs;
    @SuppressWarnings("null")
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[CHAT] " + session.receive());
        return session.send(
                session.receive()
                        .map(msg -> {
                            // String res = cs.answer(msg.getPayloadAsText());
                            String res = "服务端返回: " + msg.getPayloadAsText();
                            LOGGER.info("[RESULT]:" + res);
                            return session.textMessage(cs.answer(msg.getPayloadAsText()));
                         })
                    );
    }
}