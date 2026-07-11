package com.projects.applivo.controller;

import com.projects.applivo.emulator.AdbCommandExecutor;
import com.projects.applivo.emulator.DockerEmulatorManager;
import com.projects.applivo.emulator.EmulatorConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmulatorTestController {

    private final DockerEmulatorManager dockerEmulatorManager;
    private final AdbCommandExecutor adbCommandExecutor;
    private final EmulatorConfig emulatorConfig;

    @PostMapping("/emulator")
    public ResponseEntity<String> testEmulator() throws Exception {
        // 1. Start container
        String containerId = dockerEmulatorManager.createAndStartContainer(
                emulatorConfig.getBaseAdbPort() + 10,
                emulatorConfig.getBaseVncPort() + 10,
                emulatorConfig.getBaseConsolePort() + 10
        );

        // 2. Wait for boot
        adbCommandExecutor.waitForBoot(containerId);

        // 3. Take screenshot, return as confirmation
        byte[] screenshot = adbCommandExecutor.takeScreenshot(containerId);

        // 4. Stop container
        dockerEmulatorManager.stopAndRemoveContainer(containerId);

        return ResponseEntity.ok("Success. Screenshot bytes: " + screenshot.length);
    }
}
