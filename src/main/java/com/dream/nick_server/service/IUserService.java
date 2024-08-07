package com.dream.nick_server.service;

import com.dream.nick_server.model.User;

import reactor.core.publisher.Mono;

/**
 * 用户服务接口，定义用户管理相关的操作。
 */
public interface IUserService {
    
    /**
     * 根据用户名加载用户信息。
     * @param username 用户名
     * @return 包含用户信息的 Mono 对象
     */
    Mono<User> loadUserByUsername(String username);
    
    /**
     * 注册新用户。
     * @param username 用户名
     * @param password 密码
     * @param email 电子邮箱
     * @return 包含新用户信息的 Mono 对象
     */
    Mono<User> registerUser(String username, String password, String email);
    
    /**
     * 更新用户信息。
     * @param id 用户 ID
     * @param user 更新后的用户信息
     * @return 包含更新后用户信息的 Mono 对象
     */
    Mono<User> updateUser(String id, User user);
    
    /**
     * 删除用户。
     * @param id 用户 ID
     * @param user 用户信息
     * @return 表示删除操作结果的 Mono 对象
     */
    Mono<Boolean> deleteUser(String id, User user);
}
