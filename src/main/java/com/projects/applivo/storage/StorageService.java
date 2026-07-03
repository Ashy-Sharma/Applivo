package com.projects.applivo.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    String store(MultipartFile file, String relativeDirectory);

    void delete(String relativePath);

    Path getAbsolutePath(String relativePath);

}
