package com.dream.nick_server.service.impl;

import com.dream.nick_server.model.User;
import com.dream.nick_server.model.Authority;
import com.dream.nick_server.service.IUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements IUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String FILE_PATH = "src/main/resources/templates/user/users.json";// 存储用户数据的类路径文件
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson 对象映射器用于 JSON 处理
    private final Map<String, User> userMap = new ConcurrentHashMap<>(); // 线程安全的用户映射表

    public UserServiceImpl() {
        loadUsersFromFile(); // 服务初始化时从文件加载用户数据
    }

    /**
     * 从文件加载用户数据到内存中的用户映射表。
     */
    private void loadUsersFromFile() {
        try {
            Path path = Paths.get(FILE_PATH);
            // 检查文件是否存在
            if (Files.exists(path)) {
                // 从文件中读取用户数据并解析成 Map
                Map<String, User> loadedUsers = objectMapper.readValue(path.toFile(), new TypeReference<Map<String, User>>() {});
                // 将读取到的用户数据放入用户映射表
                userMap.putAll(loadedUsers);
            } else {
                LOGGER.error("File not found: " + FILE_PATH);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load users from file: " + FILE_PATH, e);
            e.printStackTrace();
        }
    }

    /**
     * 将内存中的用户数据保存到文件。
     */
    private synchronized void saveUsersToFile() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // 创建目录
                file.createNewFile(); // 创建文件
                LOGGER.info("Created file: " + FILE_PATH);
            }
            // 从类路径中获取用户数据文件的资源
            Path path = Paths.get(FILE_PATH);
            // 将内存中的用户数据写入到文件
            objectMapper.writeValue(path.toFile(), userMap);
            LOGGER.info("Users saved to file: " + FILE_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save users to file: " + FILE_PATH, e);
            e.printStackTrace();
        }
    }

    @Override
    public User registerUser(String username, String password, String email) {
        LOGGER.info("Registering user with username: " , username);
        for(Entry<String, User> entry : userMap.entrySet()) {
            if(entry.getValue().getUsername().equals(username)) { // 如果用户名已存在
                LOGGER.info("Username already exists: " + username);
                return null; // 注册失败
            }
        }
        // 生成随机 ID
        String id = UUID.randomUUID().toString(); // 生成随机 ID
        User user = new User(id, username, password, email, List.of(Authority.ADMIN)); // 创建用户对象
        userMap.put(id, user);
        saveUsersToFile(); // 将用户数据保存到文件
        return user;
    }

    @Override
    public User authenticate(String username, String password) {
        LOGGER.info("Authenticating user with username: {}", username);
        for(Entry<String, User> entry : userMap.entrySet()) {
            if(entry.getKey().equals(username) && entry.getValue().equals(password)) {
                LOGGER.info("User authenticated: " + username);
                return entry.getValue(); // 如果用户名和密码匹配，返回 user
            }
        }
        LOGGER.info("User authentication failed: " + username);
        return null; // 如果用户名或密码错误，返回 null
    }

    @Override
    public User updateUser(String id, User user) {
        LOGGER.info("Updating user with id: " + id);
        User oldUser = userMap.get(id);
        if(oldUser != null) { // 如果用户存在
            userMap.put(id, user); // 更新用户数据
            saveUsersToFile(); // 将更新后的数据保存到文件
            LOGGER.info("User updated: " + id);
            return user; // 返回更新后的用户对象
        }
        LOGGER.info("User update failed: " + id);
        return null; // 如果用户名或密码错误，返回 null
    }

    @Override
    public boolean deleteUser(String id, User user) {
        LOGGER.info("Deleting user with id: " + id);
        User oldUser = userMap.get(id);
        if(oldUser != null && oldUser.equals(user)) { // 如果用户存在且密码正确
            userMap.remove(id); // 删除用户数据
            saveUsersToFile(); // 将删除后的数据保存到文件
            LOGGER.info("User deleted: " + id);
            return true; // 返回 true 表示删除成功
        }
        LOGGER.info("User deletion failed: " + id);
        return false; // 如果用户名或密码错误，返回 false
    }
}
