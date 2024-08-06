package com.dream.nick_server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.dream.nick_server.handler.SuccessHandler;
import com.dream.nick_server.model.Authority;
import com.dream.nick_server.security.CustomAuthenticationConverter;
import com.dream.nick_server.security.CustomAuthenticationManager;
import com.dream.nick_server.service.impl.UserServiceImpl;
import com.dream.nick_server.websocket.echo.EchoHandler;
import com.dream.nick_server.websocket.echo.EchoServer;
import com.dream.nick_server.websocket.files.FileHandler;
import com.dream.nick_server.websocket.files.FilesManagementServer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    // 配置安全过滤链
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, CustomAuthenticationManager authenticationManager, CustomAuthenticationConverter authenticationConverter) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);
        return http
                .authorizeExchange()
                    .pathMatchers("/favicon.ico").permitAll()
                    .pathMatchers("/").permitAll()
                    .pathMatchers("/index").permitAll()
                    .pathMatchers("/user/register").permitAll()
                    .pathMatchers("/user/login").permitAll()
                    .pathMatchers("/user/authenticate").permitAll()
                    .pathMatchers("/static/**").permitAll()
                    .pathMatchers("/ws/echo").permitAll()
                    .pathMatchers("/home").hasAnyAuthority(Authority.ADMIN.getAuthority(), Authority.USER.getAuthority())
                    .anyExchange().authenticated()
                    .and()
                .csrf().disable()
                .addFilterAt(authenticationWebFilter,  SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    // 配置路由
    @Bean
    public RouterFunction<ServerResponse> routes(SuccessHandler handler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/favicon.ico"), handler::favicon)
            .andRoute(RequestPredicates.GET("/static/**"), handler::getStatic)
            .andRoute(isHttp()
                    .and(RequestPredicates.method(HttpMethod.GET))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    handler::get)
            .andRoute(isHttp()
                    .and(RequestPredicates.method(HttpMethod.POST))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    handler::post);
    }

    // 配置 WebSocket 映射
    @Bean
    public HandlerMapping webSocketMapping(final EchoHandler echoHandler, final FileHandler fileHandler) {
        LOGGER.info("webSocketMapping");
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(EchoServer.ECHO_CONNECT, echoHandler);
        map.put(FilesManagementServer.CONNECT, fileHandler);

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    // 配置 WebSocket 处理器适配器
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    // 配置密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置自定义认证管理器
    @Bean
    public CustomAuthenticationManager authenticationManager(UserServiceImpl userServiceImpl, PasswordEncoder passwordEncoder) {
        return new CustomAuthenticationManager(userServiceImpl, passwordEncoder);
    }

    // 检查请求是否为 HTTP 请求
    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
