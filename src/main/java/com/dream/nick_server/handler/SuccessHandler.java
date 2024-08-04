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

    /**
     * 处理认证成功后的逻辑
     * 
     * @param webFilterExchange WebFilterExchange 对象
     * @param authentication Authentication 对象
     * @return Mono<Void> 返回处理结果
     */
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        LOGGER.info("[Authentication Success] Authentication: {}", authentication); // 记录请求日志
        User user = (User) authentication.getPrincipal(); // 获取用户信息
        LOGGER.info("[Authentication Success] User: {}", user); // 记录用户信息
        // 处理成功请求和认证成功后的逻辑
        String redirectUrl = "/home";
        return Mono.fromRunnable(() -> {
            webFilterExchange.getExchange()
                    .getResponse()
                    .setStatusCode(HttpStatus.FOUND); // 302 重定向
            webFilterExchange.getExchange()
                    .getResponse()
                    .getHeaders()
                    .setLocation(URI.create(redirectUrl));
        });
    }

    /**
     * 处理 GET 请求
     * 根据请求路径返回对应的 HTML 页面。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> get(ServerRequest request) {
        LOGGER.info("[GET] Request path: {}", request.path());
        if (request.path().equals("/")) {
            return getHTML("index"); // 处理根路径，返回 index.html
        } else {
            return getHTML(request.path()); // 处理其他路径，返回相应的 HTML 页面
        }
    }

    /**
     * 处理 POST 请求
     * 根据请求路径返回对应的 HTML 页面。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> post(ServerRequest request) {
        LOGGER.info("[POST] Request path: {}", request.path());
        if (request.path().equals("/user/login")) {
            return userLogin(request); // 处理用户登录请求
        } else if (request.path().equals("/user/register")) {
            return userRegister(request); // 处理用户注册请求
        } else if (request.path().equals("/user/update")) {
            return userUpdate(request); // 处理用户更新请求
        } else if (request.path().equals("/user/delete")) {
            return userDelete(request); // 处理用户删除请求
        }else{
            return getHTML(request.path()); // 处理 POST 请求，返回相应的 HTML 页面
        }
    }

    /**
     * 处理用户登录请求
     * 根据请求路径返回对应的 HTML 页面，并将用户信息存储到会话中。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> userLogin(ServerRequest request) {
        String username = request.queryParam("username").orElse("");
        String password = request.queryParam("password").orElse("");
        LOGGER.info("[POST] userLogin, username: {}, password: {}", username, password); // 记录请求日志
        return request.session().flatMap(session -> {
                User user = userService.authenticate(username, password);
                if (user != null) {
                    session.getAttributes().put("id", user.getId()); // 将用户 ID 存储到会话中
                    session.getAttributes().put("user", username); // 将用户信息存储到会话中
                    session.getAttributes().put("email", user.getEmail());// 登录邮箱信息
                    session.getAttributes().put("authorities", user.getAuthorities());// 登录角色信息
                    return getHTML("home"); // 登录成功
                } else {
                    return getHTML("user/login"); // 登录失败，返回 login.html
                }
            });
    }

    /**
     * 处理用户注册请求
     * 根据请求路径返回对应的 HTML 页面，并将用户信息存储到会话中。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> userRegister(ServerRequest request) {
        String username = request.queryParam("username").orElse("");
        String password = request.queryParam("password").orElse("");
        String email = request.queryParam("email").orElse("");
        LOGGER.info("[POST] userRegister, username: {}, password: {}", username, password); // 记录请求日志
        return request.session().flatMap(session -> {
            User user = userService.registerUser(username, password, email);
            if (user != null) {
                session.getAttributes().put("id", user.getId()); // 将用户 ID 存储到会话中
                session.getAttributes().put("user", user.getUsername()); // 将用户信息存储到会话中
                session.getAttributes().put("email", user.getEmail());// 注册邮箱信息
                session.getAttributes().put("authorities", user.getAuthorities());// 注册角色信息
                return getHTML("home"); // 注册成功
            } else {
                return getHTML("user/register"); // 注册失败，返回 register.html
            }
        });
    }

    /**
     * 处理用户更新请求
     * 根据请求路径返回对应的 HTML 页面。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> userUpdate(ServerRequest request) {
        String id = request.session().attributes().get("id").toString(); // 从路径变量中提取用户 ID
        String username = request.queryParam("username").orElse("");
        String password = request.queryParam("password").orElse("");
        String email = request.queryParam("email").orElse("");
        LOGGER.info("[PUT] userUpdate, username: {}, password: {}, email: {}", username, password, email); // 记录请求日志
        User user = new User(id, username, password, email, null);
        LOGGER.info("[PUT] userUpdate, user: {}", user); // 记录用户信息
        // 执行用户更新操作
        return request.session().flatMap(session -> {
            User newUer = userService.updateUser(id, user);
            if (newUer != null) {
                session.getAttributes().put("id", id); // 将用户 ID 存储到会话中
                session.getAttributes().put("username", username); // 将用户信息存储到会话中
                session.getAttributes().put("email", newUer.getEmail());// 更新邮箱信息
                session.getAttributes().put("authorities", user.getAuthorities());// 更新角色信息
                return getHTML("home"); // 更新成功
            } else {
                return getHTML("user/update"); // 更新失败，返回 update.html
            }
        });
    }

    /**
     * 处理用户删除请求
     * 根据请求路径返回对应的 HTML 页面。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> userDelete(ServerRequest request) {
        String id = request.pathVariable("id"); // 从路径变量中提取用户 ID
        String username = request.queryParam("username").orElse("");
        String password = request.queryParam("password").orElse("");
        String email = request.queryParam("email").orElse("");
        LOGGER.info("[DELETE] userDelete, id: {}, username: {}, password: {}", id, username, password); // 记录请求日志
        // 执行用户删除操作
        User user = new User(id, username, password, email, null);
        if( userService.deleteUser(id, user)){
            return getHTML("/home"); // 删除成功，返回 home 页面
        }
        else{
            LOGGER.error("Error deleting user"); // 记录错误日志
            return getHTML("user/delete"); // 删除失败，返回 delete.html
        }

    }

    /**
     * 处理 favicon 请求
     * 返回 favicon.ico 文件。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> favicon(ServerRequest request) {
        Resource resource = new ClassPathResource("static/favicon/favicon.ico");
        return ServerResponse
                .ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving favicon: ", e); // 记录错误日志
                    return ServerResponse.notFound().build(); // 如果发生错误，返回 404
                });
    }

    /**
     * 处理静态文件请求
     * 返回指定路径的静态资源文件。
     * 
     * @param request ServerRequest 对象
     * @return Mono<ServerResponse> 返回处理结果
     */
    public Mono<ServerResponse> getStatic(ServerRequest request) {
        String path = request.path();
        Resource resource = new ClassPathResource(path);
        LOGGER.info("[GET] Static resource request for path: {}", path); // 记录请求日志
        return ServerResponse
                .ok()
                .contentType(getMediaType(path)) // 根据文件路径确定媒体类型
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving static resource: " + path, e); // 记录错误日志
                    return ServerResponse.notFound().build(); // 如果发生错误，返回 404
                });
    }

    /**
     * 根据路径返回对应的 HTML 页面
     * 
     * @param path 要返回的 HTML 页面路径
     * @return Mono<ServerResponse> 返回处理结果
     */
    private Mono<ServerResponse> getHTML(String path) {
        LOGGER.info("[GET HTML] " + path); // 记录请求日志
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .render(path) // 渲染 HTML 页面
                .onErrorResume(e -> {
                    LOGGER.error("Error rendering path: " + path, e); // 记录错误日志
                    return ServerResponse.notFound().build(); // 如果发生错误，返回 404
                });
    }

    /**
     * 根据文件路径确定媒体类型
     * 
     * @param path 文件路径
     * @return MediaType 对象
     */
    private MediaType getMediaType(String path) {
        if (path.endsWith(".js")) {
            return MediaType.valueOf("application/javascript");
        } else if (path.endsWith(".css")) {
            return MediaType.valueOf("text/css");
        } else if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (path.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (path.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else {
            return MediaType.ALL;
        }
    }
}
