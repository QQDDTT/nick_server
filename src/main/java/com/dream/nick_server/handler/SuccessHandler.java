package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.dream.nick_server.model.User;
import com.dream.nick_server.security.CustomAuthenticationManager;
import com.dream.nick_server.security.CustomAuthenticationSuccessHandler;
import com.dream.nick_server.service.impl.UserServiceImpl;

import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 处理成功请求和认证成功的处理器
 * 提供静态文件、HTML 页面服务，并处理认证成功后的重定向。
 */
@Component
public class SuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessHandler.class);

    private static final String LOGIN_PATH = "/user/login";
    private static final String REGISTER_PATH = "/user/register";
    private static final String UPDATE_PATH = "/user/update";
    private static final String DELETE_PATH = "/user/delete";
    private static final String HOME_PATH = "/home";

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private CustomAuthenticationManager customAuthenticationManager;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    /**
     * 处理 POST 请求
     */
    public Mono<ServerResponse> post(ServerRequest request) {
        LOGGER.info("[POST] Request path: {}", request.path());
        if (LOGIN_PATH.equals(request.path())) {
            return userLogin(request); // 处理登录请求
        } else if (REGISTER_PATH.equals(request.path())) {
            return userRegister(request); // 处理用户注册请求
        } else if (UPDATE_PATH.equals(request.path())) {
            return userUpdate(request); // 处理用户更新请求
        } else if (DELETE_PATH.equals(request.path())) {
            return userDelete(request); // 处理用户删除请求
        } else {
            return getHTML(request.path()); // 处理 POST 请求，返回相应的 HTML 页面
        }
    }

    /**
     * 处理登录请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> userLogin(ServerRequest request) {
        LOGGER.info("[POST] userLogin");
        return request.formData().flatMap(formData -> {
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            LOGGER.debug("[POST] userLogin, username: {}, password: {}", username, password);
            if(username == null || password == null) {
                return getHTML(LOGIN_PATH);
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
            LOGGER.debug("[POST] userLogin, authentication: {}", authentication);
            return customAuthenticationManager.authenticate(authentication)
                   .flatMap(auth -> {
                        LOGGER.info("[POST] userLogin, authentication: {}", auth);
                        WebFilterExchange webFilterExchange = new WebFilterExchange(request.exchange(), ex -> Mono.empty());
                        LOGGER.debug("[POST] userLogin, webFilterExchange: {}", webFilterExchange);
                        return customAuthenticationSuccessHandler.onAuthenticationSuccess(webFilterExchange, auth)
                                .then(ServerResponse.status(HttpStatus.FOUND)
                                        .location(URI.create(HOME_PATH))
                                        .build());
                    })
                    .onErrorResume(e -> {
                        LOGGER.error("Error logging in user: ", e);
                        return getHTML(LOGIN_PATH);
                    });
        });
    }
            

    /**
     * 处理用户注册请求
     */
    public Mono<ServerResponse> userRegister(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[POST] userRegister, username: {}, password: {}, email: {}", username, password, email);
            return userService.registerUser(username, password, email)
                    .flatMap(user -> request.session().flatMap(session -> {
                        return ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create("/home"))
                                .build();
                    }))
                    .switchIfEmpty(getHTML("user/register"));
        });
    }

    /**
     * 处理用户更新请求
     */
    public Mono<ServerResponse> userUpdate(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[PUT] userUpdate, username: {}, password: {}, email: {}", username, password, email);
            User user = new User(id, username, password, email, null);
            LOGGER.info("[PUT] userUpdate, user: {}", user);
            return userService.updateUser(id, user)
                    .flatMap(newUser -> request.session().flatMap(session -> {
                        return ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create("/home"))
                                .build();
                    }))
                    .switchIfEmpty(getHTML("user/update"));
        });
    }

    /**
     * 处理用户删除请求
     */
    public Mono<ServerResponse> userDelete(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[DELETE] userDelete, id: {}, username: {}, password: {}", id, username, password);
            User user = new User(id, username, password, email, null);
            return userService.deleteUser(id, user)
                    .flatMap(deleted -> {
                        if (deleted) {
                            return ServerResponse.status(HttpStatus.FOUND)
                                    .location(URI.create("/home"))
                                    .build();
                        } else {
                            LOGGER.error("Error deleting user");
                            return getHTML("user/delete");
                        }
                    });
        });
    }

    /**
     * 处理 GET 请求
     */
    public Mono<ServerResponse> get(ServerRequest request) {
        LOGGER.info("[GET] Request path: {}", request.path());
        if (request.path().equals("/")) {
            return getHTML("index");
        } else {
            return getHTML(request.path());
        }
    }

    /**
     * 提供网站图标
     */
    public Mono<ServerResponse> favicon(ServerRequest request) {
        Resource resource = new ClassPathResource("static/favicon/favicon.ico");
        return ServerResponse
                .ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving favicon: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 提供静态资源
     */
    public Mono<ServerResponse> getStatic(ServerRequest request) {
        String path = request.path();
        Resource resource = new ClassPathResource(path);
        LOGGER.info("[GET] Static resource request for path: {}", path);
        return ServerResponse
                .ok()
                .contentType(getMediaType(path))
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving static resource: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 提供 HTML 页面
     */
    private Mono<ServerResponse> getHTML(String path) {
        Resource resource = new ClassPathResource("/templates/" + path + ".html");
        LOGGER.info("[GET] HTML request for path: {}", path);
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving HTML resource: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 获取媒体类型
     */
    private MediaType getMediaType(String path) {
        if (path.endsWith(".js")) {
            return MediaType.valueOf("application/javascript");
        } else if (path.endsWith(".css")) {
            return MediaType.valueOf("text/css");
        } else if (path.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (path.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
