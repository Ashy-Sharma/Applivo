package com.projects.applivo.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreenshotMessage {

    @Builder.Default
    private String type = "SCREENSHOT";

    private String data;

    private Instant timestamp;

}
