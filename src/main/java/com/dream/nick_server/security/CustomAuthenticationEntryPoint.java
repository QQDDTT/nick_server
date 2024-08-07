package com.dream.nick_server.security;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 自定义认证入口点
 * 处理认证失败的情况。该类实现了 ServerAuthenticationEntryPoint 接口，
 * 在认证失败时，返回 302 状态码，并重定向到 "/user/login" 页面。
 */
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    /**
     * 处理认证失败的情况。
     *
     * @param exchange ServerWebExchange 对象，表示 HTTP 请求和响应的上下文。
     * @param e 认证异常，表示认证失败的原因。
     * @return 表示响应完成的 Mono<Void> 对象。
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        // 记录认证失败信息
        LOGGER.info("Authentication failed: {}", e.getMessage());

        // 获取 ServerHttpResponse 对象
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置 HTTP 状态码为 302（Found），表示重定向
        response.setStatusCode(HttpStatus.FOUND);
        
        // 设置重定向的目标地址为 "/user/login"
        response.getHeaders().setLocation(URI.create("/user/login"));

        // 结束响应
        return response.setComplete();
    }
}
