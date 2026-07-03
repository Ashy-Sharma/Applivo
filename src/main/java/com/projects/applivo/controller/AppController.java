package com.projects.applivo.controller;

import com.projects.applivo.dto.request.CreateAppRequest;
import com.projects.applivo.dto.request.UpdateAppRequest;
import com.projects.applivo.dto.response.AppDetailResponse;
import com.projects.applivo.dto.response.AppResponse;
import com.projects.applivo.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.projects.applivo.util.SecurityUtils.getCurrentUser;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<AppResponse> createApp(@Valid @RequestBody CreateAppRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.createApp(request, getCurrentUser()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppResponse>> getMyApps(){
        return ResponseEntity.ok(appService.getMyApps(getCurrentUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppDetailResponse> getApp(@PathVariable Long id){
        return ResponseEntity.ok(appService.getAppDetail(id, getCurrentUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<AppResponse> updateApp(@Valid @RequestBody UpdateAppRequest request,
                                                       @PathVariable Long id){
        return ResponseEntity.ok(appService.updateApp(id, request, getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<Void> deleteApp(@PathVariable Long id){
        appService.deleteApp(id, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<AppResponse> publishApp(@PathVariable Long id){
        return ResponseEntity.ok(appService.publishApp(id, getCurrentUser()));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<AppResponse> unpublishApp(@PathVariable Long id){
        return ResponseEntity.ok(appService.unpublishApp(id, getCurrentUser()));
    }


}
