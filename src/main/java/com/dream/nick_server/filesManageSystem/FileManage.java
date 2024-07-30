package com.dream.nick_server.filesManageSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManage implements Closeable{
    private static Logger LOGGER = LoggerFactory.getLogger(FileManage.class);
    private String path;
    private List<String> model;
    public static FileManage fileManage = new FileManage(null,null);
    public static final String OPEN = "file_open";
    public static final String SAVE = "file_save";
    public static final String CLOSE = "file_close";
    public static final String READE_LINE = "file_read_line";
    public static final String WRITE_LINE = "file_write_line";
    private FileManage(List<String> model,String path){
        this.model = model;
        this.path = path;
    }

    public String open(String path){
        fileManage = new FileManage(new ArrayList<>(),path);
        String result = "";
        try(BufferedReader br = new BufferedReader(new FileReader(fileManage.path))){
            br.lines().forEach(line -> {
                fileManage.model.add(line);
            });
            result = new JSONObject(fileManage.model).toString();
        }catch(IOException e){
            LOGGER.error("[OPEN ERROR] ", e);
            result = "ERROR";
        }
        return result;
    }

    public String save(){
        if(null == this.path || null == this.model){
            return "ERROR";
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileManage.path))){
            //bw.
            
        }catch(IOException e){
            LOGGER.error("[OPEN ERROR] ", e);
            return "ERROR";
        }
        return "SUCCESS";
    }

    @Override
    public void close() throws IOException {
        this.path = null;
        this.model = null;
        LOGGER.info("[CLOSE]");
    }

    public String readLine(int lineNum){
        if(null == this.path || null == this.model){
            return "ERROR";
        }else{
            this.model.get(lineNum - 1);
            return "SUCCESS";
        }
    }

    public String writeLine(int lineNum,String line){
        if(null == this.path || null == this.model){
            return "ERROR";
        }else{
            this.model.set(lineNum - 1, line);
            return "SUCCESS";
        }
    }

}
