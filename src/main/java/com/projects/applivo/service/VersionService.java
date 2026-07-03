package com.projects.applivo.service;

import com.projects.applivo.dto.request.UploadVersionRequest;
import com.projects.applivo.dto.response.AppVersionResponse;
import com.projects.applivo.dto.response.UploadResponse;
import com.projects.applivo.entity.App;
import com.projects.applivo.entity.AppVersion;
import com.projects.applivo.entity.User;
import com.projects.applivo.exception.DuplicateResourceException;
import com.projects.applivo.exception.InvalidOperationException;
import com.projects.applivo.exception.ResourceNotFoundException;
import com.projects.applivo.exception.StorageException;
import com.projects.applivo.mapper.AppVersionMapper;
import com.projects.applivo.repository.AppRepository;
import com.projects.applivo.repository.AppVersionRepository;
import com.projects.applivo.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.dnd.InvalidDnDOperationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final AppVersionMapper appVersionMapper;

    private final StorageService storageService;

    private final AppRepository appRepository;

    private final AppVersionRepository appVersionRepository;

    private App getOwnedApp(Long appId, User developer){
        return appRepository.findByIdAndDeveloper(appId, developer)
                .orElseThrow(() ->
                        new ResourceNotFoundException("App not found."));
    }

    private AppVersion getOwnedVersion(App app, Long versionId){
        return appVersionRepository.findByIdAndApp(versionId, app)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Version not found."));
    }

    @Transactional
    public UploadResponse uploadVersion(Long appId, MultipartFile file,
                                        UploadVersionRequest request,
                                        User developer){
        App app = getOwnedApp(appId, developer);
        if (appVersionRepository.existsByAppAndVersionTag(app, request.getVersionTag())){
            throw new DuplicateResourceException("Version " + request.getVersionTag() + " already exists.");
        }

        String relativeDir = app.getId() + "/" + request.getVersionTag();

        String relativePath = storageService.store(file, relativeDir);

        AppVersion version = AppVersion.builder()
                .app(app)
                .versionTag(request.getVersionTag())
                .filePath(relativePath)
                .sizeBytes(file.getSize())
                .build();

        try {
            AppVersion saved = appVersionRepository.save(version);
            return appVersionMapper.toUploadResponse(saved);
        }catch (RuntimeException e){
            try {
                storageService.delete(relativePath);
            }catch (StorageException exception){
                log.warn("Failed to clean up orphaned file : {}",
                        relativePath,
                        exception);
            }
            throw e;
        }
    }

    @Transactional
    public void deleteVersion(Long appId, Long versionId, User developer){
        App app = getOwnedApp(appId, developer);
        AppVersion version = getOwnedVersion(app, versionId);

        if (app.getIsPublished() &&
                version.getIsActive() &&
                appVersionRepository.countByAppAndIsActiveTrue(app) == 1){
            throw new InvalidOperationException(
                    "Cannot delete only active version of the published app."
            );
        }

        storageService.delete(version.getFilePath());

        appVersionRepository.delete(version);
    }

    @Transactional
    public AppVersionResponse setActiveVersion(Long appId, Long versionId, User developer){
        App app = getOwnedApp(appId, developer);
        AppVersion appVersion = getOwnedVersion(app, versionId);

        appVersion.setIsActive(true);

        List<AppVersion> versions = appVersionRepository.findByAppOrderByUploadedAtDesc(app);
        for (AppVersion curr : versions){
            curr.setIsActive(curr.getId().equals(versionId));
        }

        appVersionRepository.saveAll(versions);

        AppVersion saved = versions.stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst().orElseThrow();
        return appVersionMapper.toVersionResponse(saved);

    }

    public List<AppVersionResponse> getVersions(Long appId, User developer){
        App app = getOwnedApp(appId, developer);
        List<AppVersion> versions = appVersionRepository.findByAppOrderByUploadedAtDesc(app);
        List<AppVersionResponse> responses = new ArrayList<>();

        for (AppVersion version : versions){
            responses.add(appVersionMapper.toVersionResponse(version));
        }
        return responses;
    }





}
