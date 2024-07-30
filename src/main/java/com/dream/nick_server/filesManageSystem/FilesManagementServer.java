package com.dream.nick_server.filesManageSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FilesManagementServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesManagementServer.class);
    public static final String CONNECT = "/files_connect";
    public static final String MSG_KEY = "message";
    public static final String PATH_KEY = "path";
    public static final String COND_KEY = "cond";
    public static final String VALUE_KEY = "value";
    public static final String LINE_KEY = "lineNum";
    private String msg;
    private String path;
    private String cond;
    private String value;
    private int lineNum;

    public String getMsg(String json){
        try{
            JSONObject jsonObject = new JSONObject(json);
            this.msg = jsonObject.getString(MSG_KEY);
            this.path = jsonObject.getString(PATH_KEY);
            this.cond = jsonObject.getString(COND_KEY);
            this.value = jsonObject.getString(VALUE_KEY);
            this.lineNum = Integer.parseInt(jsonObject.getString(LINE_KEY));
            LOGGER.info("[MESSAGE] " + msg);
            switch(msg){
                case PathManage.EACH:
                    return PathManage.pathManage.each();
                case PathManage.SEARCH:
                    return PathManage.pathManage.search(cond);
                case PathManage.CREATE:
                    return PathManage.pathManage.create(path);
                case PathManage.DELETE:
                    return PathManage.pathManage.delete(path);
                case FileManage.OPEN:
                    return FileManage.fileManage.open(path);
                case FileManage.SAVE:
                    return FileManage.fileManage.save();
                case FileManage.CLOSE:
                    return "FILE CLOSE";
                case FileManage.READE_LINE:
                    return FileManage.fileManage.readLine(0);
                case FileManage.WRITE_LINE:
                    return FileManage.fileManage.writeLine(lineNum, value);
                default:
                    return "MESSAGE ERROR";
            }
        }catch(Exception e){
            LOGGER.error("[ERROR]", e);
            return "ERROR";
        }


    }

}
