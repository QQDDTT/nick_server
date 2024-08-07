package com.dream.nick_server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.dream.nick_server.model.User;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JwtTokenProvider 类，用于生成和验证 JWT 令牌，并从令牌中获取认证信息。
 */
@Component
public class JwtTokenProvider {

    // 用于签名 JWT 的密钥
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // 令牌的过期时间，1 天（以毫秒为单位）
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * 生成 JWT 令牌。
     *
     * @param authentication 认证信息
     * @return 生成的 JWT 令牌
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username) // 设置令牌的主题（通常是用户名）
                .setIssuedAt(now) // 设置令牌的签发时间
                .setExpiration(expiryDate) // 设置令牌的过期时间
                .signWith(key) // 使用密钥进行签名
                .compact(); // 构建并压缩成 JWT 字符串
    }

    /**
     * 从 JWT 令牌中获取认证信息。
     *
     * @param token JWT 令牌
     * @return 认证信息
     */
    public Mono<Authentication> getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key) // 设置用于解析的签名密钥
                .parseClaimsJws(token) // 解析 JWT 令牌
                .getBody();
        String username = claims.getSubject(); // 获取用户名

        // 从服务中检索实际的 UserDetails 实例
        User user = new User("", username, "", "", List.of());
        return Mono.just(new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities()));
    }

    /**
     * 验证 JWT 令牌。
     *
     * @param token JWT 令牌
     * @return 如果令牌有效，返回 true；否则，返回 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(key) // 设置用于解析的签名密钥
                .parseClaimsJws(token); // 解析 JWT 令牌
            return true;
        } catch (Exception e) {
            return false; // 令牌无效
        }
    }
}
