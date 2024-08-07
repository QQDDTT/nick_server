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

/**
 * 自定义认证管理器
 * 该类实现了 ReactiveAuthenticationManager 接口，用于处理用户认证。
 * 它依赖于 UserServiceImpl 和 PasswordEncoder 两个 Bean 来进行认证。
 */
@Component
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationManager.class);

    private final UserServiceImpl userService; // 用户服务，用于加载用户
    private final PasswordEncoder passwordEncoder; // 密码编码器，用于密码匹配

    /**
     * 构造函数注入用户服务和密码编码器。
     *
     * @param userService 用户服务实现类
     * @param passwordEncoder 密码编码器
     */
    public CustomAuthenticationManager(UserServiceImpl userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 进行用户认证。
     *
     * @param authentication 认证信息，包含用户名和密码
     * @return 包含认证结果的 Mono 对象
     * @throws AuthenticationException 认证异常
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // 获取用户名
        String password = (String) authentication.getCredentials(); // 获取密码

        LOGGER.info("Authenticating user: {}", username); // 记录认证过程的日志

        // 从用户服务中加载用户
        return userService.loadUserByUsername(username)
               .flatMap(user -> {
                    // 使用密码编码器验证密码是否匹配
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        // 创建认证成功的 Authentication 对象
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            user, // 用户对象
                            user.getPassword(), // 用户密码
                            user.getAuthorities() // 用户权限
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
