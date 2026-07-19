package com.projects.applivo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_versions")
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @Column(name = "version_tag", nullable = false,length = 50)
    private String versionTag;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "uploaded_at", insertable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "package_name", length = 255)
    private String packageName;

    @Column(name = "compatibility_warning", length = 500)
    private String compatibilityWarning;

    @OneToMany(mappedBy = "appVersion", fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();

}
