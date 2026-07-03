package com.projects.applivo.controller;

import com.projects.applivo.dto.response.AppDetailResponse;
import com.projects.applivo.dto.response.AppResponse;
import com.projects.applivo.entity.App;
import com.projects.applivo.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/apps")
@RequiredArgsConstructor
public class PublicController {

    private final AppService appService;

    @GetMapping
    public ResponseEntity<Page<AppResponse>> getPublishedApps(Pageable pageable){
        return ResponseEntity.ok(appService.getPublishedApps(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppDetailResponse> getPublishedApp(@PathVariable Long id){
        return ResponseEntity.ok(appService.getPublishedApp(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AppResponse>> searchApps(@RequestParam String query, Pageable pageable){
        return ResponseEntity.ok(appService.searchApps(query, pageable));
    }

}
