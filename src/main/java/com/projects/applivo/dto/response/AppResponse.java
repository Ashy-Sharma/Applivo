package com.projects.applivo.dto.response;

import lombok.*;

import java.time.Instant;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AppResponse {

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

    private Long versionCount;

}
