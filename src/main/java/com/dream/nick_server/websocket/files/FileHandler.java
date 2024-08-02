package com.dream.nick_server.websocket.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

@Component
public class FileHandler implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    @Autowired
    private FilesManagementServer fms;

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[File Socket] Connection Established"); // 连接建立日志

        return session.send(
                session.receive()
                        .doOnNext(msg -> LOGGER.info("[Received Message]: {}", msg.getPayloadAsText())) // 记录接收到的消息
                        .map(msg -> {
                            String response = fms.getMsg(msg.getPayloadAsText()); // 获取处理后的响应消息
                            LOGGER.debug("[Response]: {}", response); // 记录响应消息
                            return session.textMessage(response); // 发送响应消息
                        })
        );
    }
}
