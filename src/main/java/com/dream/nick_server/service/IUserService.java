package com.dream.nick_server.service;

import com.dream.nick_server.model.User;

public interface IUserService {
    User authenticate(String username, String password);
    User registerUser(String username, String password, String email);
    User updateUser(String id, User user);
    boolean deleteUser(String id, User user);
}
