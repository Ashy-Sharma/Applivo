package com.projects.applivo.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@Getter
public class StorageProperties {

    @Value("${storage.base-upload-path}")
    private String baseUploadPath;

    @Value("${storage.max-file-size-mb}")
    private long maxFileSizeMb;

    @Value("${storage.allowed-extensions}")
    private List<String> allowedExtensions;

    @PostConstruct
    public void init(){
        Path uploadPath = Paths.get(baseUploadPath);
        try{
            Files.createDirectories(uploadPath);
        }catch (IOException e){
            throw new IllegalStateException("Failed to create upload directory : " + baseUploadPath, e);
        }
    }

}
