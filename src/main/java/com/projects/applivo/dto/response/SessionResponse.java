package com.projects.applivo.dto.response;

import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SessionResponse {

    private Long sessionId;

    private Long appVersionId;

    private String status;

    private String wsEndpoint;

    private String subscriptionTopic;

    private Instant startedAt;

    private int screenWidth;

    private int screenHeight;

}
