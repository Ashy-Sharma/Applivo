package com.projects.applivo.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponse {

    private Long versionId;

    private String versionTag;

    private Long sizeByBytes;

    private Boolean isActive;

    private Instant uploadedAt;

}
