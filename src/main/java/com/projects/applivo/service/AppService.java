package com.projects.applivo.service;

import com.projects.applivo.dto.request.CreateAppRequest;
import com.projects.applivo.dto.request.UpdateAppRequest;
import com.projects.applivo.dto.response.AppDetailResponse;
import com.projects.applivo.dto.response.AppResponse;
import com.projects.applivo.entity.App;
import com.projects.applivo.entity.User;
import com.projects.applivo.exception.DuplicateResourceException;
import com.projects.applivo.exception.InvalidOperationException;
import com.projects.applivo.exception.ResourceNotFoundException;
import com.projects.applivo.mapper.AppMapper;
import com.projects.applivo.repository.AppRepository;
import com.projects.applivo.repository.AppVersionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppMapper appMapper;

    private final AppVersionRepository appVersionRepository;

    private final AppRepository appRepository;

    @Transactional
    public AppResponse createApp(CreateAppRequest request, User developer){

        if(appRepository.existsByNameAndDeveloper(request.getName(), developer)){
            throw new DuplicateResourceException("App already exists!");
        }

        App app = appMapper.toEntity(request, developer);

        App saved = appRepository.save(app);

        return appMapper.toResponse(saved);
    }

    public List<AppResponse> getMyApps(User developer){
        List<App> apps = appRepository.findByDeveloper(developer);
        return apps.stream().map(appMapper::toResponse).toList();
    }

    public AppDetailResponse getAppDetail(Long appId, User developer){
        App app = getOwnedApp(appId, developer);
        return appMapper.toDetailResponse(app);
    }

    @Transactional
    public AppResponse updateApp(Long appId, UpdateAppRequest request, User developer){
        App app = getOwnedApp(appId, developer);

        if (request.getName() != null
                && !request.getName().equals(app.getName())
                && appRepository.existsByNameAndDeveloper(request.getName(), developer)) {

            throw new DuplicateResourceException(
                    "You already have an app named " + request.getName() + " .");
        }

        if (request.getDescription() != null){
            app.setDescription(request.getDescription());
        }
        if (request.getName() != null){
            app.setName(request.getName());
        }
        if (request.getCategory() != null){
            app.setCategory(request.getCategory());
        }
        if (request.getIconUrl() != null){
            app.setIconUrl(request.getIconUrl());
        }
        App saved = appRepository.save(app);
        return appMapper.toResponse(saved);
    }

    @Transactional
    public void deleteApp(Long appId, User developer){
        App app = getOwnedApp(appId, developer);
        appRepository.delete(app);
    }

    @Transactional
    public AppResponse publishApp(Long appId, User developer){
        App app = getOwnedApp(appId, developer);
        if (appVersionRepository.findByAppAndIsActiveTrue(app).isEmpty()){
            throw new InvalidOperationException("Cannot publish app without an active version.");
        }

        app.setIsPublished(true);
        App saved = appRepository.save(app);
        return appMapper.toResponse(saved);
    }

    @Transactional
    public AppResponse unpublishApp(Long appId, User developer){
        App app = getOwnedApp(appId, developer);
        app.setIsPublished(false);
        App saved = appRepository.save(app);
        return appMapper.toResponse(saved);
    }


    public Page<AppResponse> getPublishedApps(Pageable pageable) {
        return appRepository.findByIsPublishedTrue(pageable)
                .map(appMapper::toResponse);
    }

    public AppDetailResponse getPublishedApp(Long appId){
        App app = appRepository.findByIdAndIsPublishedTrue(appId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("App not found."));
        return appMapper.toDetailResponse(app);
    }

    public Page<AppResponse> searchApps(String query, Pageable pageable){
        return appRepository.searchPublishedApps(query, pageable)
                .map(appMapper::toResponse);
    }


    private App getOwnedApp(Long appId, User developer){
        return appRepository.findByIdAndDeveloper(appId, developer)
                .orElseThrow(() -> new ResourceNotFoundException("App not found."));
    }





}
