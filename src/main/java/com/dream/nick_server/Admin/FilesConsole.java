package com.dream.nick_server.Admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FilesConsole {
    private final Logger LOGGER = LoggerFactory.getLogger(FilesConsole.class); 

    private static final String BASE_PATH = "./src/main";
    public static final String PATH_VAR = "FilesConsole_path";
    public static final String READ = "FilesConsole_read";
    public static final String WRITE = "FilesConsole_write";
    public static final String OPEN = "FilesConsole_open";
    public static final String SAVE = "FilesConsole_save";

    public String func(String msg){
        String result = "";
        if(READ.equals(msg)){
            result = read();
        }
        return result;
    }
    private String read(){
        FilesMode filesMode = new FilesMode();
        try{
            Files.walk(Paths.get(BASE_PATH))
                .forEach(path -> filesMode.add(path.toString()));
        }catch(IOException e){
            LOGGER.error( "FILES WALK:",e);
        }
        return filesMode.get();
    }
}

class FilesMode{
    private List<String> files;
    FilesMode(){
        this.files = new ArrayList<String>();
    }
    void add(String path){
        this.files.add(path);
    }
    String get(){
        return this.files.toString();
    }
}
