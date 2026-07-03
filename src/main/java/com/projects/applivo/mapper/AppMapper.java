package com.projects.applivo.mapper;

import com.projects.applivo.dto.request.CreateAppRequest;
import com.projects.applivo.dto.response.AppDetailResponse;
import com.projects.applivo.dto.response.AppResponse;
import com.projects.applivo.entity.App;
import com.projects.applivo.entity.User;
import com.projects.applivo.repository.AppRepository;
import com.projects.applivo.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppMapper {

    private final AppVersionMapper appVersionMapper;

    private final AppVersionRepository appVersionRepository;

    public AppResponse toResponse(App app){
        return AppResponse.builder()
                .id(app.getId())
                .iconUrl(app.getIconUrl())
                .name(app.getName())
                .isPublished(app.getIsPublished())
                .description(app.getDescription())
                .updatedAt(app.getUpdatedAt())
                .category(app.getCategory())
                .createdAt(app.getCreatedAt())
                .developerId(app.getDeveloper().getId())
                .developerUsername(app.getDeveloper().getUsername())
                .versionCount(appVersionRepository.countByApp(app))
                .build();
    }

    public AppDetailResponse toDetailResponse(App app){
        return AppDetailResponse.builder()
                .id(app.getId())
                .iconUrl(app.getIconUrl())
                .name(app.getName())
                .isPublished(app.getIsPublished())
                .description(app.getDescription())
                .updatedAt(app.getUpdatedAt())
                .category(app.getCategory())
                .createdAt(app.getCreatedAt())
                .developerId(app.getDeveloper().getId())
                .developerUsername(app.getDeveloper().getUsername())
                .versionCount(app.getVersions().size())
                .versions(app.getVersions()
                        .stream()
                        .map(appVersionMapper::toVersionResponse)
                        .toList()
                )
                .build();
    }

    public App toEntity(CreateAppRequest request, User developer){
        return App.builder()
                .developer(developer)
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .category(request.getCategory())
                .build();
    }


}
