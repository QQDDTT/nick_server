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

/**
 * 自定义认证转换器，用于从 HTTP 请求中提取用户名和密码，并创建 UsernamePasswordAuthenticationToken 对象。
 * 该转换器实现了 ServerAuthenticationConverter 接口，支持在 Reactive 编程模型中进行认证。
 */
@Component
public class CustomAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationConverter.class);

    /**
     * 将 ServerWebExchange 转换为 Authentication 对象。
     *
     * @param exchange ServerWebExchange 对象，表示 HTTP 请求和响应的上下文。
     * @return 包含 Authentication 对象的 Mono，如果转换失败则返回空的 Mono。
     */
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // 检查请求方法是否为 POST
        if (HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
            // 获取请求中的表单数据
            return exchange.getFormData()
                .flatMap(data -> {
                    // 从表单数据中提取用户名和密码
                    String username = data.getFirst("username");
                    String password = data.getFirst("password");
                    
                    // 检查用户名和密码是否存在
                    if (username != null && password != null) {
                        LOGGER.info("Username and password found in the request");
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
