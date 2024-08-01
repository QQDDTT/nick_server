package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.dream.nick_server.filesManageSystem.FilesManagementServer;

import reactor.core.publisher.Mono;

@Component
public class FileHandler implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    @Autowired
    private FilesManagementServer fms;

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[文件 SOCKET] 连接建立");

        return session.send(
                session.receive()
                        .doOnNext(msg -> LOGGER.info("[接收到的消息]: {}", msg.getPayloadAsText()))
                        .map(msg -> {
                            String response = fms.getMsg(msg.getPayloadAsText());
                            LOGGER.info("[响应]: {}", response);
                            return session.textMessage(response);
                        })
        );
    }
}
