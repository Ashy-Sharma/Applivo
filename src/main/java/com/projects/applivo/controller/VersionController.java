package com.projects.applivo.controller;

import com.projects.applivo.dto.request.UploadVersionRequest;
import com.projects.applivo.dto.response.AppVersionResponse;
import com.projects.applivo.dto.response.UploadResponse;
import com.projects.applivo.service.VersionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.projects.applivo.util.SecurityUtils.getCurrentUser;

@RestController
@RequestMapping("/api/apps/{appId}/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;


    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<UploadResponse> uploadVersion(
            @RequestPart("file") MultipartFile file,
            @RequestParam("versionTag") @NotBlank @Size(max = 50) String versionTag,
            @PathVariable Long appId) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        versionService.uploadVersion(
                                appId,
                                file,
                                versionTag,
                                getCurrentUser()
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<AppVersionResponse>> getVersions(@PathVariable Long appId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        versionService.getVersions(appId, getCurrentUser())
                );
    }

    @DeleteMapping("/{versionId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long appId, @PathVariable Long versionId){
        versionService.deleteVersion(appId, versionId, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{versionId}/activate")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<AppVersionResponse> setVersionActive(@PathVariable Long appId, @PathVariable Long versionId){
        return ResponseEntity
                .ok(versionService.setActiveVersion(appId, versionId, getCurrentUser()));
    }

}
