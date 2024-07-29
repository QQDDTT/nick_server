package com.dream.nick_server.handler;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.dream.nick_server.filesManageSystem.FilesCommandMessage;
import com.dream.nick_server.filesManageSystem.FilesManagement;
import reactor.core.publisher.Mono;

@Component
public class FileHandler implements WebSocketHandler{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
    @Autowired
    private FilesManagement filesManagement;
    @SuppressWarnings("null")
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[FILE SOCKET]:" + session.getId());
        return session.send(
                session.receive()
                        .map(msg -> {
                            JSONObject msgJson = new JSONObject(msg.getPayloadAsText());
                            Map<String,String> data = filesManagement.wf(FilesCommandMessage.valueOf(msgJson.getString("enum")),msgJson.getString("path"),msgJson.getString("value"));
                            LOGGER.debug(data.toString());
                            return session.textMessage(new JSONObject(data).toString());
                        })
                    );
    }
}
