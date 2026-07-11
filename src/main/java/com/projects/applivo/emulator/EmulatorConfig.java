package com.projects.applivo.emulator;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@Component
public class EmulatorConfig {

    @Value("${emulator.docker-image}")
    private String dockerImage;

    @Value("${emulator.boot-timeout-seconds}")
    private int bootTimeoutSeconds;

    @Value("${emulator.max-concurrent-sessions}")
    private int maxConcurrentSessions;

    @Value("${emulator.screenshot-android-path}")
    private String screenshotAndroidPath;

    @Value("${emulator.screenshot-container-path}")
    private String screenshotContainerPath;

    @Value("${emulator.base-console-port}")
    private int baseConsolePort;

    @Value("${emulator.base-adb-port}")
    private int baseAdbPort;

    @Value("${emulator.base-vnc-port}")
    private int baseVncPort;

}
