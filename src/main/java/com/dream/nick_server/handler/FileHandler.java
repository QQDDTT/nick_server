package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.dream.nick_server.Admin.FilesConsole;

import reactor.core.publisher.Mono;

@Component
public class FileHandler implements WebSocketHandler{
        private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
    @Autowired
    private FilesConsole filesConsole;
    @SuppressWarnings("null")
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[FILE SOCKET]:" + session.getId());
        return session.send(
                session.receive()
                        .map(msg -> session.textMessage(filesConsole.func(msg.getPayloadAsText())))
                    );
    }
}
