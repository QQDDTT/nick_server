package com.dream.nick_server.websocket.files;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dream.nick_server.websocket.WebSocketMessageBody;

@Component
public class PathManage implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathManage.class);

    // 常量定义，用于标识不同的文件操作
    public static final String BASE_PATH = ".\\src\\main\\workSpace";
    public static final PathManage pathManage = new PathManage();
    public static final String EACH = "path_each";
    public static final String SEARCH = "path_search";
    public static final String CREATE = "path_create";
    public static final String DELETE = "path_delete";
    public static final String END = "path_end";

    private Map<String, String> model;

    // 单例模式实例
    private static PathManage instance;

    // 私有构造函数
    private PathManage() {
        this.model = new TreeMap<>();
    }

    // 获取单例实例的方法
    public static PathManage getInstance() {
        if (instance == null) {
            synchronized (PathManage.class) {
                if (instance == null) {
                    instance = new PathManage();
                }
            }
        }
        return instance;
    }

    /**
     * 遍历指定路径下的所有文件，并将其文件名和路径添加到模型中
     * 
     * @param path 文件夹路径
     * @return 操作结果的 JSON 字符串
     */
    public String each() {
        model.clear();
        try {
            Files.walk(Paths.get(BASE_PATH))
                // .filter(p -> !Files.isDirectory(p))
                .forEach(p -> model.put(p.toFile().getName(), p.toString()));
                LOGGER.debug("PathManage each model: " + model);
            return WebSocketMessageBody.success("", EACH, model);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("[EACH ERROR]:", e);
            return WebSocketMessageBody.error("", EACH, null);
        }
    }

    /**
     * 在指定的基础路径下根据条件搜索文件
     * 
     * @param cond 文件名的匹配条件
     * @return 操作结果的 JSON 字符串
     */
    public String search(String cond) {
        model.clear();
        try {
            Files.walk(Paths.get(BASE_PATH))
                // .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().matches(cond))
                .forEach(p -> model.put(p.toFile().getName(), p.toString()));
            return WebSocketMessageBody.success("", SEARCH, model);
        } catch (IOException e) {
            LOGGER.error("[SEARCH ERROR]:", e);
            return WebSocketMessageBody.error("", SEARCH, null);
        }
    }

    /**
     * 创建文件或目录
     * 
     * @param path 要创建的文件或目录路径
     * @return 操作结果的 JSON 字符串
     */
    public String create(String path) {
        if (Files.exists(Paths.get(path))) {
            return WebSocketMessageBody.error("", CREATE, null);
        } else if (path.matches("\\.\\w+$")) {
            try {
                Files.createFile(Path.of(path));
                model.put(path.substring(path.lastIndexOf("\\") + 1), path);
                return WebSocketMessageBody.success("", CREATE, model);
            } catch (IOException e) {
                return WebSocketMessageBody.error("", CREATE, null);
            }
        } else {
            try {
                Files.createDirectories(Path.of(path));
                model.put(path.substring(path.lastIndexOf("\\") + 1), path);
                return WebSocketMessageBody.success("", CREATE, model);
            } catch (IOException e) {
                return WebSocketMessageBody.error("", CREATE, model);
            }
        }
    }

    /**
     * 删除指定路径的文件，并将其重命名为 .bk
     * 
     * @param path 要删除的文件路径
     * @return 操作结果的 JSON 字符串
     */
    public String delete(String path) {
        if (Files.exists(Paths.get(path)) && !Files.exists(Paths.get(path + ".bk"))) {
            File file = new File(path);
            file.renameTo(new File(path + ".bk"));
            model.remove(file.getName());
            return WebSocketMessageBody.success("", DELETE, model);
        } else {
            return WebSocketMessageBody.error("", DELETE, model);
        }
    }

    /**
     * 结束路径管理操作并清理资源
     * 
     * @return 操作结果的 JSON 字符串
     */
    public String end() {
        LOGGER.info("PathManage end");
        try {
            this.close();
            return WebSocketMessageBody.success("", END, null);
        } catch (IOException e) {
            LOGGER.error("PathManage close error:", e);
            return WebSocketMessageBody.error("", END, null);
        }
    }

    @Override
    public void close() throws IOException {
        this.model.clear();
    }
}