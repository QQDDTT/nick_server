package com.dream.nick_server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.dream.nick_server.service.IUserService;
import com.dream.nick_server.websocket.echo.EchoHandler;
import com.dream.nick_server.websocket.echo.EchoServer;
import com.dream.nick_server.websocket.files.FileHandler;
import com.dream.nick_server.websocket.files.FilesManagementServer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 配置 Spring Security 的过滤链
     * 
     * @param http ServerHttpSecurity 对象
     * @param successHandler 自定义登录成功处理器
     * @return SecurityWebFilterChain
     */
    @SuppressWarnings("removal")
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, SuccessHandler successHandler) {
        return http
                .authorizeExchange()
                    .pathMatchers("/favicon.ico").permitAll() // 允许访问 favicon
                    .pathMatchers("/").permitAll() // 允许访问首页
                    .pathMatchers("/index").permitAll() // 允许访问index页面
                    .pathMatchers("/user/register").permitAll() // 允许访问register页面
                    .pathMatchers("/user/login").permitAll() // 允许访问login页面
                    .pathMatchers("/user/authenticate").permitAll()
                    .pathMatchers("/static/**").permitAll() // 允许访问静态资源
                    .pathMatchers("/ws/echo").permitAll() // 允许访问 Echo WebSocket
                    .pathMatchers("/home").hasAnyAuthority(Authority.ADMIN.getAuthority(), Authority.USER.getAuthority()) // 允许访问 home 页面（需要管理员或用户权限）
                    .anyExchange().authenticated() // 其他路径需要认证
                    .and()
                .formLogin()
                    .loginPage("/user/login") // 自定义登录页面
                    .authenticationSuccessHandler(successHandler) // 自定义登录成功处理器
                    .and()
                .csrf().disable() // 禁用 CSRF 保护（根据需要配置）
                .build();
    }

    /**
     * 配置用户相关的路由
     * 
     * @param userService 用户服务
     * @return RouterFunction<ServerResponse>
     */
    @Bean
public RouterFunction<ServerResponse> routes(IUserService userService, SuccessHandler handler) {
    return RouterFunctions
        // 其他静态资源和 HTML 页面路由
        .route(RequestPredicates.GET("/favicon.ico"), handler::favicon)
        .andRoute(RequestPredicates.GET("/static/**"), handler::getStatic)
        .andRoute(isHttp()
                .and(RequestPredicates.method(HttpMethod.GET))
                .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                handler::get)
        .andRoute(isHttp()
                .and(RequestPredicates.method(HttpMethod.POST))
                .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                handler::post)
                ;
}


    /**
     * 配置 WebSocket 映射
     * 
     * @param echoHandler Echo 处理器
     * @param fileHandler 文件处理器
     * @return HandlerMapping
     */
    @Bean
    public HandlerMapping webSocketMapping(final EchoHandler echoHandler, final FileHandler fileHandler) {
        LOGGER.info("webSocketMapping");
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(EchoServer.ECHO_CONNECT, echoHandler); // 映射 Echo 处理器
        map.put(FilesManagementServer.CONNECT, fileHandler); // 映射文件管理处理器

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE); // 设置优先级
        mapping.setUrlMap(map); // 设置 URL 到 WebSocket 处理器的映射
        return mapping;
    }

    /**
     * 配置 WebSocket 处理器适配器
     * 
     * @return WebSocketHandlerAdapter
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    /**
     * 检查请求是否为 HTTP 请求
     * 
     * @return RequestPredicate
     */
    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
