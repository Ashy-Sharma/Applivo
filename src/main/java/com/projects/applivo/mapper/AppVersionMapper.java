package com.projects.applivo.mapper;

import com.projects.applivo.dto.response.AppVersionResponse;
import com.projects.applivo.dto.response.UploadResponse;
import com.projects.applivo.entity.AppVersion;
import org.springframework.stereotype.Component;

@Component
public class AppVersionMapper {

    public UploadResponse toUploadResponse(AppVersion appVersion){
        return UploadResponse.builder()
                .uploadedAt(appVersion.getUploadedAt())
                .versionTag(appVersion.getVersionTag())
                .versionId(appVersion.getId())
                .sizeBytes(appVersion.getSizeBytes())
                .message("Apk uploaded successfully.")
                .compatibilityWarning(appVersion.getCompatibilityWarning())
                .build();
    }


    public AppVersionResponse toVersionResponse(AppVersion appVersion){
        return AppVersionResponse.builder()
                .uploadedAt(appVersion.getUploadedAt())
                .versionTag(appVersion.getVersionTag())
                .versionId(appVersion.getId())
                .sizeByBytes(appVersion.getSizeBytes())
                .isActive(appVersion.getIsActive())
                .build();
    }

}
