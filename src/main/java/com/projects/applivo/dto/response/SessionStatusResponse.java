package com.projects.applivo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SessionStatusResponse {

    private Long sessionId;

    private String status;

    private String message;

    private Integer screenWidth;

    private Integer screenHeight;

}
