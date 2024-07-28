package com.dream.nick_server.Admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FilesConsole {
    private final Logger LOGGER = LoggerFactory.getLogger(FilesConsole.class); 

    private static final String BASE_PATH = "./src/main";
    private static final String TEMPLATE_PATH = "./src/main/resources/templates";
    public static final String PATH_VAR = "FilesConsole_path";

    public static final String READ_TEMPLATE = "FilesConsole_read_template";
    public static final String READ = "FilesConsole_read";
    public static final String WRITE = "FilesConsole_write";
    public static final String OPEN = "FilesConsole_open";
    public static final String SAVE = "FilesConsole_save";

    public Map<String,String> func(String msg){
        LOGGER.info("[MESSAGE] " + msg);
        switch (msg) {
            case READ:
                return read();
            case READ_TEMPLATE:  
                return readTemplate();
            default:
                return read();
        }
    }
    private Map<String,String> read(){
        Map<String,String> filesMode = new HashMap<>();
        try{
            Files.walk(Paths.get(BASE_PATH))
                .forEach(path -> filesMode.put(path.toString(),"@"));
        }catch(IOException e){
            LOGGER.error( "FILES WALK:",e);
        }
        return filesMode;
    }
    private Map<String,String> readTemplate(){
        Map<String,String> filesMode = new HashMap<>();
        try{
            Files.walk(Paths.get(TEMPLATE_PATH))
                .filter(path -> !Files.isDirectory(path))
                // .filter(path -> path.endsWith(".html"))
                .forEach(path -> {
                    filesMode.put(path.toString(), path.toString().substring(TEMPLATE_PATH.length(), path.toString().length() - 5));
                });
        }catch(IOException e){
            LOGGER.error( "FILES WALK:",e);
        }
        return filesMode;
    }

}

