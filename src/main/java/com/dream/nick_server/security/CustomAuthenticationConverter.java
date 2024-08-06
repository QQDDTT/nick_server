package com.dream.nick_server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationConverter.class);

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // 检查请求方法是否为 POST
        if (HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
            return exchange.getFormData()
                .flatMap(data -> {
                    String username = data.getFirst("username");
                    String password = data.getFirst("password");
                    if (username != null && password != null) {
                        LOGGER.info("请求中找到用户名和密码");
                        // 如果用户名和密码都存在，则创建并返回一个 UsernamePasswordAuthenticationToken 对象
                        return Mono.just(new UsernamePasswordAuthenticationToken(username, password));
                    }
                    // 如果用户名或密码为空，则返回空的 Mono
                    return Mono.empty();
                });
        }
        // 如果请求方法不是 POST，返回空的 Mono
        return Mono.empty();
    }
}
