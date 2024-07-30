package com.dream.nick_server.filesManageSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathManage {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathManage.class);
    public static final String BASE_PATH = ".\\src\\main";
    public static final PathManage pathManage = new PathManage();
    public static final String EACH = "path_each";
    public static final String SEARCH = "path_search";
    public static final String CREATE = "path_create";
    public static final String DELETE = "path_delete";

    private PathManage(){} 

    public String each(){
        return each(BASE_PATH);
    }
     
    public String each(String path){
        Map<String,String> model = new HashMap<>();
        try{
            Files.walk(Paths.get(path))
                .filter(p -> !Files.isDirectory(p))
                .forEach(p -> model.put(p.toFile().getName(),p.toString()));
            return new JSONObject(model).toString();
        }catch(IOException e){
            LOGGER.error( "[EACH ERROR]:",e);
            return "ERROR";
        }
    }
    public String search(String cond){
        Map<String,String> model = new HashMap<>();
        try{
            Files.walk(Paths.get(BASE_PATH))
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().matches(cond))
                .forEach(p -> model.put(p.toFile().getName(),p.toString()));
            return new JSONObject(model).toString();
        }catch(IOException e){
            LOGGER.error( "[SEARCH ERROR]:",e);
            return "ERROR";
        }
    }

    public String create(String path){
        if(Files.exists(Paths.get(path))){
            return "ERROR";
        }else if(path.matches("\\.\\w+$")){
            try {
                Files.createFile(Path.of(path));
                return "SECCESS";
            } catch (IOException e) {
                return "ERROR";
            }
        }else{
            try {
                Files.createDirectories(Path.of(path));
                return "SECCESS";
            } catch (IOException e) {
                return "ERROR";
            }
        }
    }

    public String delete(String path){
        if(Files.exists(Paths.get(path)) && !Files.exists(Paths.get(path + ".bk"))){
            File file = new File(path);
            file.renameTo(new File(path + ".bk"));
            return "SECCESS";
        }else{
            return "ERROR";
        }
    }
}
