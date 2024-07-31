package com.dream.nick_server.chat;

import org.springframework.stereotype.Component;

@Component
public class ChatServer {
    public static final String CHAT_CONNECT = "/chat_connect";

    public String answer(String msg){
        return "服务端返回: " + msg;
    }
}
