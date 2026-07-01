package com.projects.applivo.repository;

import com.projects.applivo.entity.RefreshToken;
import com.projects.applivo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String token);

    List<RefreshToken> findAllByUserAndIsRevokedFalse(User user);

    @Modifying
    @Query("update RefreshToken r set r.isRevoked = true where r.isRevoked = false and r.user = :user ")
    void revokeAllByUser(@Param("user") User user);

}