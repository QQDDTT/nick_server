package com.dream.nick_server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * JwtAuthenticationFilter 类，用于处理 JWT 认证。
 * 该类实现了 WebFilter 接口，用于在请求处理链中进行 JWT 认证。
 * 该类从请求中提取 JWT token，并使用 JwtTokenProvider 进行认证，如果认证成功，
 * 则将认证信息写入上下文并继续过滤链，否则继续过滤链。
 * 如果没有找到 token，则继续过滤链。
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 构造函数，初始化 JwtTokenProvider。
     *
     * @param jwtTokenProvider 用于验证和解析 JWT 令牌的提供者
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 过滤方法处理每个请求，尝试提取和验证 JWT 令牌。
     * 如果令牌有效，它将设置认证上下文，并继续处理请求。
     *
     * @param exchange 当前的服务器交换
     * @param chain    web 过滤链
     * @return 当过滤链完成时完成的 Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 从请求中提取 JWT 令牌
        String token = extractToken(exchange);
        if (token != null) {
            LOGGER.info("Token found: {}", token);
            // 使用 JWT 令牌进行认证
            return jwtTokenProvider.getAuthentication(token)
                    .flatMap(authentication -> {
                        LOGGER.info("Authentication successful for token: {}", token);
                        // 将认证信息写入上下文并继续过滤链
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    })
                    .onErrorResume(e -> {
                        LOGGER.error("Authentication error: {}", e.getMessage());
                        // 如果认证失败，继续过滤链
                        return chain.filter(exchange);
                    });
        } else {
            LOGGER.info("No token found in request");
            // 如果没有找到令牌，继续过滤链
            return chain.filter(exchange);
        }
    }

    /**
     * 从请求中提取 JWT 令牌。
     *
     * @param exchange 当前的服务器交换
     * @return 如果存在则返回 JWT 令牌，否则返回 null
     */
    private String extractToken(ServerWebExchange exchange) {
        // 从请求头中获取 Authorization 字段
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        // 检查 Authorization 字段是否以 "Bearer " 开头
        if (header != null && header.startsWith("Bearer ")) {
            // 提取令牌部分
            return header.substring(7);
        }
        return null;
    }
}
