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
import com.dream.nick_server.security.CustomAuthenticationEntryPoint;
import com.dream.nick_server.security.CustomAuthenticationManager;
import com.dream.nick_server.security.JwtAuthenticationFilter;
import com.dream.nick_server.service.impl.UserServiceImpl;
import com.dream.nick_server.websocket.echo.EchoHandler;
import com.dream.nick_server.websocket.echo.EchoServer;
import com.dream.nick_server.websocket.files.FileHandler;
import com.dream.nick_server.websocket.files.FilesManagementServer;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全配置类，用于配置 Spring Security 相关的设置。
 */
@Configuration
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 配置安全过滤链。
     * 
     * @param http ServerHttpSecurity 对象，用于配置安全设置。
     * @param authenticationManager 自定义认证管理器。
     * @param authenticationConverter 自定义认证转换器。
     * @param authenticationEntryPoint 自定义认证入口点。
     * @param jwtAuthenticationFilter JWT 认证过滤器。
     * @return 配置好的 SecurityWebFilterChain 对象。
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, 
                                                      CustomAuthenticationManager authenticationManager, 
                                                      CustomAuthenticationConverter authenticationConverter,
                                                      CustomAuthenticationEntryPoint authenticationEntryPoint,
                                                      JwtAuthenticationFilter jwtAuthenticationFilter) {
        // 创建认证过滤器并设置认证转换器
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);
        
        return http
                .authorizeExchange()
                    .pathMatchers("/favicon.ico").permitAll() // 允许所有人访问 favicon
                    .pathMatchers("/").permitAll() // 允许所有人访问主页
                    .pathMatchers("/index").permitAll() // 允许所有人访问 index 页面
                    .pathMatchers("/user/register").permitAll() // 允许所有人访问注册页面
                    .pathMatchers("/user/login").permitAll() // 允许所有人访问登录页面
                    .pathMatchers("/user/authenticate").permitAll() // 允许所有人访问认证接口
                    .pathMatchers("/static/**").permitAll() // 允许所有人访问静态资源
                    .pathMatchers("/ws/echo").permitAll() // 允许所有人访问 WebSocket 连接
                    .pathMatchers("/home").hasAnyAuthority(Authority.ADMIN.getAuthority(), Authority.USER.getAuthority()) // 需要特定权限
                    .anyExchange().authenticated() // 其他所有请求都需要认证
                    .and()
                .csrf().disable() // 禁用 CSRF 保护
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint) // 使用自定义的认证入口点
                    .and()
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION) // 添加 JWT 认证过滤器
                .build();
    }

    /**
     * 配置自定义认证入口点。
     * 
     * @return CustomAuthenticationEntryPoint 对象。
     */
    @Bean
    public CustomAuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    /**
     * 配置路由功能。
     * 
     * @param handler SuccessHandler 对象，用于处理不同的请求。
     * @return 配置好的 RouterFunction 对象。
     */
    @Bean
    public RouterFunction<ServerResponse> routes(SuccessHandler handler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/favicon.ico"), handler::favicon) // 处理 favicon 请求
            .andRoute(RequestPredicates.GET("/static/**"), handler::getStatic) // 处理静态资源请求
            .andRoute(isHttp()
                    .and(RequestPredicates.method(HttpMethod.GET))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    handler::get) // 处理 GET 请求
            .andRoute(isHttp()
                    .and(RequestPredicates.method(HttpMethod.POST))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    handler::post); // 处理 POST 请求
    }

    /**
     * 配置 WebSocket 映射。
     * 
     * @param echoHandler EchoHandler 对象，用于处理 WebSocket 连接。
     * @param fileHandler FileHandler 对象，用于处理文件管理 WebSocket 连接。
     * @return 配置好的 HandlerMapping 对象。
     */
    @Bean
    public HandlerMapping webSocketMapping(final EchoHandler echoHandler, final FileHandler fileHandler) {
        LOGGER.info("Configuring WebSocket mapping");
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(EchoServer.ECHO_CONNECT, echoHandler); // 配置 Echo WebSocket 处理器
        map.put(FilesManagementServer.CONNECT, fileHandler); // 配置文件管理 WebSocket 处理器

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE); // 设置映射优先级
        mapping.setUrlMap(map); // 设置 URL 到处理器的映射
        return mapping;
    }

    /**
     * 配置 WebSocket 处理器适配器。
     * 
     * @return WebSocketHandlerAdapter 对象。
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    /**
     * 配置密码编码器。
     * 
     * @return PasswordEncoder 对象。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置自定义认证管理器。
     * 
     * @param userServiceImpl UserServiceImpl 对象，用于加载用户。
     * @param passwordEncoder PasswordEncoder 对象，用于密码编码。
     * @return CustomAuthenticationManager 对象。
     */
    @Bean
    public CustomAuthenticationManager authenticationManager(UserServiceImpl userServiceImpl, PasswordEncoder passwordEncoder) {
        return new CustomAuthenticationManager(userServiceImpl, passwordEncoder);
    }

    /**
     * 检查请求是否为 HTTP 请求。
     * 
     * @return RequestPredicate 对象。
     */
    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
