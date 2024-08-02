package com.dream.nick_server.websocket.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dream.nick_server.websocket.WebSocketMessageBody;

@Component
public class FileManage implements Closeable{
    private static Logger LOGGER = LoggerFactory.getLogger(FileManage.class);

    // 常量定义，用于标识不同的文件操作
    public static final String OPEN = "file_open";
    public static final String SAVE = "file_save";
    public static final String END = "file_end";
    public static final String READE_LINE = "file_read_line";
    public static final String WRITE_LINE = "file_write_line";

    private String path; // 文件路径
    private Map<String, String> model; // 存储文件内容的 Map
    private Lock lock = new ReentrantLock(); // 锁，用于保证线程安全

    // 单例模式实例
    private static FileManage instance;

    // 私有构造函数
    private FileManage() {
        this.model = new TreeMap<>();
    }

    // 获取单例实例的方法
    public static FileManage getInstance() {
        if (instance == null) {
            synchronized (FileManage.class) {
                if (instance == null) {
                    instance = new FileManage();
                }
            }
        }
        return instance;
    }

    /**
     * 打开指定路径的文件，并读取其内容到内存中
     * @param path 文件路径
     * @return 操作结果的 JSON 字符串
     */
    public String open(String path) {
        lock.lock();
        try {
            LOGGER.info("[OPEN] " + this.path);
            this.path = path;
            this.model.clear();
            String result;
            try (BufferedReader br = new BufferedReader(new FileReader(this.path))) {
                String line;
                int lineNumber = 1;
                while ((line = br.readLine()) != null) {
                    this.model.put(String.valueOf(lineNumber++), line);
                }
                result = WebSocketMessageBody.success("", OPEN, model);
            } catch (IOException e) {
                LOGGER.error("[OPEN ERROR] ", e);
                result = WebSocketMessageBody.error("", OPEN, null);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将内存中的内容保存到文件中
     * @return 操作结果的 JSON 字符串
     */
    public String save() {
        lock.lock();
        try {
            LOGGER.info("[SAVE] " + this.path);
            String result;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.path))) {
                for (String line : this.model.values()) {
                    bw.write(line);
                    bw.newLine();
                }
                result = WebSocketMessageBody.success("", SAVE, null);
            } catch (IOException e) {
                LOGGER.error("[SAVE ERROR] ", e);
                result = WebSocketMessageBody.error("", SAVE, null);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 结束文件操作，并关闭文件
     * @return 操作结果的 JSON 字符串
     */
    public String end(){
        try {
            this.close();
        } catch (IOException e) {
            return WebSocketMessageBody.error("", END, null);
        }
        return WebSocketMessageBody.success("", END, null);
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            this.path = null;
            this.model.clear();
            LOGGER.info("[CLOSE]");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 读取指定行的内容
     * @param line 行号
     * @return 操作结果的 JSON 字符串
     */
    public String readLine(String line) {
        lock.lock();
        try {
            LOGGER.info("[READ_LINE] " + line);
            String text = this.model.get(line);
            if (text == null) {
                return WebSocketMessageBody.error("", READE_LINE, null);
            }
            return WebSocketMessageBody.success("", READE_LINE, Map.of(line, text));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 写入内容到指定行
     * @param line 行号
     * @param text 写入的内容
     * @return 操作结果的 JSON 字符串
     */
    public String writeLine(String line, String text) {
        lock.lock();
        try {
            LOGGER.info("[WRITE_LINE] " + line);
            this.model.put(line, text);
            return WebSocketMessageBody.success("", WRITE_LINE, null);
        } finally {
            lock.unlock();
        }
    }
}
