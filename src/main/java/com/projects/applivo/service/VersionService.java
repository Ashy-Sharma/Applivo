package com.projects.applivo.service;

import com.projects.applivo.dto.request.UploadVersionRequest;
import com.projects.applivo.dto.response.AppVersionResponse;
import com.projects.applivo.dto.response.UploadResponse;
import com.projects.applivo.emulator.EmulatorConfig;
import com.projects.applivo.entity.App;
import com.projects.applivo.entity.AppVersion;
import com.projects.applivo.entity.User;
import com.projects.applivo.exception.DuplicateResourceException;
import com.projects.applivo.exception.InvalidFileException;
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
import net.dongliu.apk.parser.ApkFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final AppVersionMapper appVersionMapper;

    private final StorageService storageService;

    private final AppRepository appRepository;

    private final AppVersionRepository appVersionRepository;

    private final VersionPersistenceService versionPersistenceService;

    private final EmulatorConfig emulatorConfig;

    private static final Set<String> KNOWN_ABI_DIRS = Set.of(
            "armeabi-v7a", "arm64-v8a", "x86", "x86_64", "armeabi", "mips", "mips64"
    );

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

    public UploadResponse uploadVersion(Long appId, MultipartFile file,
                                        String versionTag,
                                        User developer){
        App app = getOwnedApp(appId, developer);
        if (appVersionRepository.existsByAppAndVersionTag(app, versionTag)){
            throw new DuplicateResourceException("Version " + versionTag + " already exists.");
        }

        String relativeDir = app.getId() + "/" + versionTag;
        String relativePath = storageService.store(file, relativeDir);
        Path absoluteFile = storageService.getAbsolutePath(relativePath);

        String packageName;
        try {
            try (ApkFile apkFile = new ApkFile(absoluteFile.toFile())) {
                packageName = apkFile.getApkMeta().getPackageName();
            }
            if (packageName == null || packageName.isBlank()) {
                throw new InvalidFileException("Could not determine package name from APK.");
            }
        } catch (InvalidFileException e) {
            storageService.delete(relativePath);
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse APK manifest for {}", relativePath, e);
            storageService.delete(relativePath);
            throw new InvalidFileException("Uploaded file is not a valid APK.");
        }

        String compatibilityWarning = checkAbiCompatibility(absoluteFile);

        try {
            return versionPersistenceService.saveVersion(
                    app, versionTag, relativePath, file.getSize(), packageName, compatibilityWarning);
        } catch (RuntimeException e) {
            try {
                storageService.delete(relativePath);
            } catch (StorageException ex) {
                log.warn("Failed to clean up orphaned file: {}", relativePath, ex);
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


    private String checkAbiCompatibility(Path apkPath) {
        Set<String> apkAbis = new HashSet<>();

        try (ZipFile zip = new ZipFile(apkPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("lib/") && name.endsWith(".so")) {
                    String[] parts = name.split("/");
                    if (parts.length >= 2 && KNOWN_ABI_DIRS.contains(parts[1])) {
                        apkAbis.add(parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to inspect APK native libraries: {}", apkPath, e);
            return null;
        }

        if (apkAbis.isEmpty()) {
            return null;
        }

        boolean compatible = apkAbis.stream().anyMatch(emulatorConfig.getSupportedAbis()::contains);
        if (!compatible) {
            return "This APK contains native libraries for " + apkAbis
                    + ", but the emulator supports " + emulatorConfig.getSupportedAbis()
                    + ". It may fail to install or run.";
        }
        return null;
    }

}