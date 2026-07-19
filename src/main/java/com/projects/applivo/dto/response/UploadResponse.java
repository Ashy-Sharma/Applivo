package com.projects.applivo.dto.response;

import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@Builder
@Getter
public class UploadResponse {

    private Long versionId;

    private String versionTag;

    private Long sizeBytes;

    private Instant uploadedAt;

    private String message;

    private String compatibilityWarning;

}
