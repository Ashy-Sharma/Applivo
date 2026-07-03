package com.projects.applivo.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AppDetailResponse {

    private Long id;

    private String name;

    private String description;

    private String category;

    private String iconUrl;

    private Boolean isPublished;

    private Instant createdAt;

    private Instant updatedAt;

    private Long developerId;

    private String developerUsername;

    private int versionCount;

    private List<AppVersionResponse> versions;
}
