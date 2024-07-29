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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FilesManagement {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesManagement.class);
    public static final String BASE_PATH = ".\\src\\main";
    public static final String URL = "/file_connect";
    public Map<String,String> wf(FilesCommandMessage msg,String path,String value){
        switch(msg){
            case EACH_FILES:
                return each(path);
            case SEARCH_FILES:
                return search(path,value);
            case READ_FILE:
                return read(path);
            case WRITE_FILE:
                return write(path,value);
            case CREATE_FILE:
                return create(path,value);
            case DELETE_FILE:
                return delete(path);
            default:
                return Map.of("ERROR",msg.name() + path);
        }

    }

    private Map<String,String> each(String path){
        Map<String,String> model = new HashMap<>();
        try{
            Files.walk(Paths.get(BASE_PATH))
                .filter(p -> !Files.isDirectory(p))
                .forEach(p -> model.put(p.toFile().getName(),p.toString()));
        }catch(IOException e){
            LOGGER.error( "[FILES EACH]:",e);
        }
        return model;
    }

    private Map<String,String> search(String path,String cond){
        Map<String,String> model = new HashMap<>();
        if("".equals(path)){
            path = BASE_PATH;
        }
        try{
            Files.walk(Paths.get(path))
                .filter(p -> p.toString().matches(cond))
                .forEach(p -> model.put(p.toFile().getName(),p.toString()));
        }catch(IOException e){
            LOGGER.error( "[FILES SEARCH]:",e);
        }
        return model;
    }

    private Map<String,String> read(String path){
        Map<String,String> model = new HashMap<>();
        try{
            Files.readAllLines(Paths.get(path),StandardCharsets.UTF_8)
                .forEach(line -> model.put(String.valueOf(model.size() + 1),line));
        }catch(IOException e){
            LOGGER.error( "[FILES SEARCH]:",e);
        }
        return model;
    }
    private Map<String,String> write(String path,String value){
        Map<String,String> model = new HashMap<>();
        try{
            Files.write(Paths.get(path), List.of(value.split("\n")), StandardCharsets.UTF_8,StandardOpenOption.WRITE);
        }catch(IOException e){
            LOGGER.error( "[FILES WRITE]:",e);
        }
        return model;
    }
    private Map<String,String> create(String path,String value){
        Map<String,String> model = new HashMap<>();
        try{
            if(Files.isDirectory(Paths.get(path),LinkOption.NOFOLLOW_LINKS) && Files.notExists(Paths.get(path + "\\" + value))){
                if(value.matches("\\.")){
                    Files.createFile(Paths.get(path + "\\" + value));
                }else{
                    Files.createDirectory(Paths.get(path + "\\" + value));
                }
            }
        }catch(IOException e){
            LOGGER.error( "[FILES CREATE]:",e);
        }
        return model;
    }
    private Map<String,String> delete(String path){
        Map<String,String> model = new HashMap<>();
        try{
            Files.move(Paths.get(path),Paths.get(path + ".bk"));
        }catch(IOException e){
            LOGGER.error( "[FILES DELETE]:",e);
        }
        return model;
    }
}
