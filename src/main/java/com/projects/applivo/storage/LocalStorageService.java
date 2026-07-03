package com.projects.applivo.storage;

import com.projects.applivo.config.StorageProperties;
import com.projects.applivo.exception.GlobalExceptionHandler;
import com.projects.applivo.exception.InvalidFileException;
import com.projects.applivo.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService{

    private final StorageProperties storageProperties;

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Override
    public String store(MultipartFile file, String relativeDirectory) {

        validateFile(file);

        String filename = Paths.get(file.getOriginalFilename())
                .getFileName()
                .toString();
        String randomFilename = UUID.randomUUID() + "_" + filename;

        Path relativePath = Paths.get(relativeDirectory).resolve(randomFilename);
        Path absolutePath = getAbsolutePath(relativePath.toString());

        try {

            Files.createDirectories(absolutePath.getParent());

            Files.copy(
                    file.getInputStream(),
                    absolutePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return relativePath.toString();
        }catch (IOException e){
            throw new StorageException("Failed to store file.", e);
        }

    }

    @Override
    public void delete(String relativePath) {
        Path absoluteFilePath = getAbsolutePath(relativePath);
        try {

            Files.delete(absoluteFilePath);

        }catch (NoSuchFileException e){
            log.warn("File not found during deletion: {}", absoluteFilePath);        }catch (IOException e){
            throw new StorageException("Failed to delete file.", e);
        }
    }

    @Override
    public Path getAbsolutePath(String relativePath) {
        return Paths.get(storageProperties.getBaseUploadPath())
                .resolve(relativePath)
                .normalize();
    }


    private void validateFile(MultipartFile file) {

        if(file.isEmpty()){
            throw new InvalidFileException("File is empty");
        }

        String extension = getExtension(file.getOriginalFilename());

        if(!storageProperties.getAllowedExtensions().contains(extension)){
            throw new InvalidFileException("File type not supported.");
        }


        if(file.getSize() > storageProperties.getMaxFileSizeMb() * 1024L * 1024L){
            throw new InvalidFileException("File is too large.");
        }

    }

    private String getExtension(String filename){
        if (filename == null || filename.isBlank()){
            throw new InvalidFileException("Filename is missing.");
        }

        int lastDot = filename.lastIndexOf('.');

        if (lastDot == -1 || lastDot == filename.length()-1){
            throw new InvalidFileException("Invalid extension.");
        }

        return filename.substring(lastDot).toLowerCase();
    }
}
