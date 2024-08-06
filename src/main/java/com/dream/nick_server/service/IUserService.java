package com.dream.nick_server.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.dream.nick_server.model.User;

public interface IUserService extends UserDetailsService{
    User registerUser(String username, String password, String email);
    User updateUser(String id, User user);
    boolean deleteUser(String id, User user);
}
