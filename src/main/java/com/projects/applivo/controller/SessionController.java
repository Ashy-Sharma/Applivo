package com.projects.applivo.controller;

import com.projects.applivo.dto.request.CreateSessionRequest;
import com.projects.applivo.dto.response.SessionResponse;
import com.projects.applivo.dto.response.SessionStatusResponse;
import com.projects.applivo.service.SessionService;
import com.projects.applivo.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request){

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(sessionService.createSession(request, SecurityUtils.getCurrentUser()));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(
            @PathVariable Long id){

        return ResponseEntity.ok(sessionService.getSessionStatus(id, SecurityUtils.getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id){
        sessionService.endSession(id, SecurityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

}
