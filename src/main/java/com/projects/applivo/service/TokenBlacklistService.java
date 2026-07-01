package com.projects.applivo.service;

import com.projects.applivo.entity.BlacklistedToken;
import com.projects.applivo.repository.BlacklistedTokenRepository;
import com.projects.applivo.security.JwtService;
import com.projects.applivo.util.HashUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final JwtService jwtService;

    @Transactional
    public void blacklist(String rawToken){
        String hashedToken = HashUtil.sha256(rawToken);
        Instant expiry = jwtService.extractExpiration(rawToken);
        if (expiry.isBefore(Instant.now()) || blacklistedTokenRepository.existsByTokenHash(hashedToken)){
            return;
        }
        BlacklistedToken newToken = BlacklistedToken.builder()
                .tokenHash(hashedToken)
                .expiresAt(expiry)
                .revokedAt(Instant.now())
                .build();
        blacklistedTokenRepository.save(newToken);
    }

    public boolean isBlacklisted(String rawToken){
        String hashedToken = HashUtil.sha256(rawToken);
        return blacklistedTokenRepository.existsByTokenHash(hashedToken);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpired(){
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

}
