package com.dream.nick_server.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChatServer {
    public static final String CHAT_CONNECT = "/chat_connect";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    public String answer(String msg){
        LOGGER.info("[MESSAGE]:" + msg);
        return "服务端返回: " + msg;
    }
}
