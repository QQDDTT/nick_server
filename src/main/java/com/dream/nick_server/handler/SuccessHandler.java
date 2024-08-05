package com.dream.nick_server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;

import com.dream.nick_server.model.User;
import com.dream.nick_server.service.impl.UserServiceImpl;

import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 处理成功请求和认证成功的处理器
 * 提供静态文件、HTML 页面服务，并处理认证成功后的重定向。
 */
@Component
public class SuccessHandler implements ServerAuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessHandler.class);

    @Autowired
    private UserServiceImpl userService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        LOGGER.info("[Authentication Success] Authentication: {}", authentication);
        User user = (User) authentication.getPrincipal();
        LOGGER.info("[Authentication Success] User: {}", user);
        return handleAuthenticationSuccess(webFilterExchange, user);
    }

    public Mono<ServerResponse> post(ServerRequest request) {
        LOGGER.info("[POST] Request path: {}", request.path());
        if (request.path().equals("/user/register")) {
            return userRegister(request); // 处理用户注册请求
        } else if (request.path().equals("/user/update")) {
            return userUpdate(request); // 处理用户更新请求
        } else if (request.path().equals("/user/delete")) {
            return userDelete(request); // 处理用户删除请求
        } else {
            return getHTML(request.path()); // 处理 POST 请求，返回相应的 HTML 页面
        }
    }

    public Mono<ServerResponse> userRegister(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[POST] userRegister, username: {}, password: {}, email: {}", username, password, email);
            User user = userService.registerUser(username, password, email);
            if (user != null) {
                return request.session().flatMap(session -> {
                    storeUserInSession(session, user);
                    return ServerResponse.status(HttpStatus.FOUND)
                            .location(URI.create("/home"))
                            .build();
                });
            } else {
                return getHTML("user/register");
            }
        });
    }

    public Mono<ServerResponse> userUpdate(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[PUT] userUpdate, username: {}, password: {}, email: {}", username, password, email);
            User user = new User(id, username, password, email, null);
            LOGGER.info("[PUT] userUpdate, user: {}", user);
            User newUser = userService.updateUser(id, user);
            if (newUser != null) {
                return request.session().flatMap(session -> {
                    storeUserInSession(session, newUser);
                    return ServerResponse.status(HttpStatus.FOUND)
                            .location(URI.create("/home"))
                            .build();
                });
            } else {
                return getHTML("user/update");
            }
        });
    }

    public Mono<ServerResponse> userDelete(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.info("[DELETE] userDelete, id: {}, username: {}, password: {}", id, username, password);
            User user = new User(id, username, password, email, null);
            if (userService.deleteUser(id, user)) {
                return ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/home"))
                        .build();
            } else {
                LOGGER.error("Error deleting user");
                return getHTML("user/delete");
            }
        });
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        LOGGER.info("[GET] Request path: {}", request.path());
        if (request.path().equals("/")) {
            return getHTML("index");
        } else {
            return getHTML(request.path());
        }
    }

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

    private Mono<ServerResponse> getHTML(String path) {
        Resource resource = new ClassPathResource("/templates" + path + ".html");
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

    private void storeUserInSession(WebSession session, User user) {
        session.getAttributes().put("id", user.getId());
        session.getAttributes().put("user", user.getUsername());
        session.getAttributes().put("email", user.getEmail());
        session.getAttributes().put("authorities", user.getAuthorities());
    }

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

    private Mono<Void> handleAuthenticationSuccess(WebFilterExchange webFilterExchange, User user) {
        return webFilterExchange.getExchange().getSession()
                .flatMap(session -> {
                    storeUserInSession(session, user);
                    webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                    webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/home"));
                    return webFilterExchange.getExchange().getResponse().setComplete();
                });
    }
}
