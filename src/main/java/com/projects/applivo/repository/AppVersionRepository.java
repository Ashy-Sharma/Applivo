package com.projects.applivo.repository;

import com.projects.applivo.entity.App;
import com.projects.applivo.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    Optional<AppVersion> findByIdAndApp(Long id, App app);

    boolean existsByAppAndVersionTag(App app, String versionTag);

    long countByAppAndIsActiveTrue(App app);

    List<AppVersion> findByAppOrderByUploadedAtDesc(App app);

    Optional<AppVersion> findByAppAndIsActiveTrue(App app);

    long countByApp(App app);

    @Modifying
    @Query("update AppVersion v set v.isActive = false where v.app = :app and v.isActive = true")
    void deactivateAllForApp(@Param("app") App app);
}
