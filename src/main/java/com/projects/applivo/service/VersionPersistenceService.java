package com.projects.applivo.service;

import com.projects.applivo.dto.response.UploadResponse;
import com.projects.applivo.entity.App;
import com.projects.applivo.entity.AppVersion;
import com.projects.applivo.mapper.AppVersionMapper;
import com.projects.applivo.repository.AppVersionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VersionPersistenceService {

    private final AppVersionRepository appVersionRepository;
    private final AppVersionMapper appVersionMapper;

    @Transactional
    public UploadResponse saveVersion(App app, String versionTag,
                                      String relativePath, long sizeBytes) {

        appVersionRepository.deactivateAllForApp(app);

        AppVersion version = AppVersion.builder()
                .app(app)
                .versionTag(versionTag)
                .filePath(relativePath)
                .sizeBytes(sizeBytes)
                .build();

        AppVersion saved = appVersionRepository.save(version);
        return appVersionMapper.toUploadResponse(saved);
    }
}
