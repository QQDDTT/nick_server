package com.dream.nick_server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dream.nick_server.service.impl.UserServiceImpl;

import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationManager.class);

    private final UserServiceImpl userService; // 用户服务
    private final PasswordEncoder passwordEncoder; // 密码编码器

    // 构造函数注入用户服务和密码编码器
    public CustomAuthenticationManager(UserServiceImpl userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // 获取用户名
        String password = (String) authentication.getCredentials(); // 获取密码

        LOGGER.info("Authenticating user: {}", username); // 记录认证过程的日志

        return Mono.fromCallable(() -> userService.loadUserByUsername(username)) // 从用户服务中加载用户
               .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) { // 验证密码
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            user,
                            user.getPassword(),
                            user.getAuthorities() // 设置用户的权限
                        );
                        return Mono.just(auth); // 返回认证成功的对象
                    } else {
                        return Mono.empty(); // 返回空的 Mono 表示认证失败
                    }
               })
               .doOnError(error -> LOGGER.error("Error authenticating user: {}", username, error)) // 记录认证失败的错误日志
               .onErrorResume(error -> Mono.empty()); // 发生错误时返回空的 Mono
    }
}
