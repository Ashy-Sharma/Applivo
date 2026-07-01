package com.projects.applivo.controller;

import com.projects.applivo.dto.request.LoginRequest;
import com.projects.applivo.dto.request.LogoutRequest;
import com.projects.applivo.dto.request.RefreshTokenRequest;
import com.projects.applivo.dto.request.RegisterRequest;
import com.projects.applivo.dto.response.AuthResponse;
import com.projects.applivo.entity.User;
import com.projects.applivo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       @RequestHeader("Authorization") String authHeader){

        String jwtToken = authHeader.substring(7);
        authService.logout(request.getRefreshToken(), jwtToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String authHeader){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String jwtToken = authHeader.substring(7);
        authService.logoutAll(user, jwtToken);
        return ResponseEntity.noContent().build();
    }

}
