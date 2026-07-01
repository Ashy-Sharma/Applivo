package com.projects.applivo.repository;

import com.projects.applivo.entity.BlacklistedToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    void deleteByExpiresAtBefore(Instant expiresAtBefore);

    Optional<BlacklistedToken> findByTokenHash(String tokenHash);
}
