package com.dream.nick_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.dream.nick_server.handler.SuccessHandler;
import com.dream.nick_server.websocket.echo.EchoHandler;
import com.dream.nick_server.websocket.echo.EchoServer;
import com.dream.nick_server.websocket.files.FileHandler;
import com.dream.nick_server.websocket.files.FilesManagementServer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RouterConfig {

    /**
     * 配置 HTTP 路由
     * 
     * @param handler 处理成功和静态文件请求的处理器
     * @return 路由函数
     */
    @Bean
    public RouterFunction<?> routeCity(SuccessHandler handler) {
        return RouterFunctions
                // 处理 favicon 请求
                .route(RequestPredicates.GET("/favicon.ico"), handler::favicon)
                // 处理静态文件请求
                .andRoute(RequestPredicates.GET("/static/**"), handler::getStatic)
                // 处理 GET 请求并接受 HTML
                .andRoute(isHttp()
                        .and(RequestPredicates.method(HttpMethod.GET))
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                        handler::get)
                // 处理 POST 请求并接受 HTML
                .andRoute(isHttp()
                        .and(RequestPredicates.method(HttpMethod.POST))
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                        handler::post);
    }

    /**
     * 配置 WebSocket 路由
     * 
     * @param echoHandler Echo 处理器
     * @param fileHandler 文件管理处理器
     * @return WebSocket 路由映射
     */
    @Bean
    public HandlerMapping webSocketMapping(final EchoHandler echoHandler, final FileHandler fileHandler) {
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(EchoServer.ECHO_CONNECT, echoHandler);
        map.put(FilesManagementServer.CONNECT, fileHandler);

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    /**
     * WebSocket 处理器适配器
     * 
     * @return WebSocket 处理器适配器
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    /**
     * 判断请求是否是 HTTP 或 HTTPS 协议
     * 
     * @return 请求谓词
     */
    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
