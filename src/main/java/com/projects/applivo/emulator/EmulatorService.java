package com.projects.applivo.emulator;

import com.projects.applivo.entity.*;
import com.projects.applivo.exception.EmulatorException;
import com.projects.applivo.repository.AppVersionRepository;
import com.projects.applivo.repository.EmulatorInstanceRepository;
import com.projects.applivo.repository.SessionRepository;
import com.projects.applivo.storage.StorageService;
import com.projects.applivo.websocket.InteractionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.nio.file.Path;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmulatorService {

    private final DockerEmulatorManager dockerEmulatorManager;

    private final AdbCommandExecutor adbCommandExecutor;

    private final EmulatorConfig emulatorConfig;

    private final EmulatorInstanceRepository emulatorInstanceRepository;

    private final SessionRepository sessionRepository;

    private final StorageService storageService;

    private final AppVersionRepository appVersionRepository;

    private final EmulatorPersistenceService persistenceService;

    @Async
    public void startEmulatorAsync(Long sessionId, Long appVersionId) {

        Long emulatorId = null;
        String containerId = null;

        try {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EmulatorException("Session not found"));
            AppVersion appVersion = appVersionRepository.findById(appVersionId)
                    .orElseThrow(() -> new EmulatorException("AppVersion not found"));

            if (dockerEmulatorManager.getRunningContainerCount()
                    >= emulatorConfig.getMaxConcurrentSessions()) {
                persistenceService.updateSessionStatus(sessionId, SessionStatus.FAILED);
                return;
            }

            int consolePort = allocatePort(emulatorConfig.getBaseConsolePort());
            int adbPort = allocatePort(emulatorConfig.getBaseAdbPort());
            int vncPort = allocatePort(emulatorConfig.getBaseVncPort());

            containerId = dockerEmulatorManager.createAndStartContainer(
                    consolePort, adbPort, vncPort);

            EmulatorInstance emulator = EmulatorInstance.builder()
                    .session(session)
                    .containerId(containerId)
                    .adbPort(adbPort)
                    .vncPort(vncPort)
                    .lastAccessAt(Instant.now())
                    .status(EmulatorStatus.STARTING)
                    .build();

            EmulatorInstance saved = persistenceService.saveEmulatorInstance(emulator);
            emulatorId = saved.getId();

            persistenceService.updateSessionContainerId(sessionId, containerId);

            adbCommandExecutor.waitForBoot(containerId);

            Path apkPath = storageService.getAbsolutePath(appVersion.getFilePath());
            dockerEmulatorManager.copyFileToContainer(containerId, apkPath, "/tmp");
            adbCommandExecutor.installApk(containerId, "/tmp/" + apkPath.getFileName());

            persistenceService.updateEmulatorStatus(emulatorId, EmulatorStatus.RUNNING);
            persistenceService.updateSessionStatus(sessionId, SessionStatus.ACTIVE);

            log.info("Emulator {} started successfully for session {}.", containerId, sessionId);

        } catch (Exception e) {
            log.error("Failed to start emulator for session {}.", sessionId, e);
            persistenceService.updateSessionStatus(sessionId, SessionStatus.FAILED);
            if (emulatorId != null) {
                persistenceService.updateEmulatorStatus(emulatorId, EmulatorStatus.FAILED);
            }
            if (containerId != null) {
                try {
                    dockerEmulatorManager.stopAndRemoveContainer(containerId);
                } catch (Exception ignored) {}
            }
        }
    }


    public void stopEmulator(String containerId){

        EmulatorInstance emulatorInstance = emulatorInstanceRepository
                .findByContainerId(containerId)
                .orElseThrow(() -> new EmulatorException("Emulator not found : " + containerId));

        persistenceService.updateEmulatorStatus(
                emulatorInstance.getId(),
                EmulatorStatus.STOPPED
        );

        dockerEmulatorManager.stopAndRemoveContainer(containerId);

    }

    public void handleInput(String containerId, InteractionMessage message){
        switch (message.getType()){
            case TAP -> {
                adbCommandExecutor.tap(containerId, message.getX(), message.getY());
            }
            case SWIPE -> {
                adbCommandExecutor.swipe(
                        containerId,
                        message.getX(),
                        message.getY(),
                        message.getX2(),
                        message.getY2(),
                        message.getDuration()
                );
            }
            case TEXT -> {
                adbCommandExecutor.inputText(containerId, message.getText());
            }
            case KEY -> {
                adbCommandExecutor.pressKey(containerId, message.getKeyCode());
            }
            default -> {
                throw new EmulatorException("Unsupported interaction type : " + message.getType());
            }

        }
    }

    public byte[] captureScreenshot(String containerId){
        return adbCommandExecutor.takeScreenshot(containerId);
    }

    private int allocatePort(int basePort){
        int maxInUse = emulatorInstanceRepository.findMaxAdbPort(emulatorConfig.getBaseAdbPort());
        int slot = (maxInUse - emulatorConfig.getBaseAdbPort())/ 2 + 1;
        return basePort + (slot*2);
    }

    private void failSession(Long sessionId,
                             Long emulatorId,
                             String containerId,
                             String reason){
        try {
            persistenceService.updateSessionStatus(
                    sessionId,
                    SessionStatus.FAILED
            );

            if (emulatorId != null){
                persistenceService.updateEmulatorStatus(
                        emulatorId,
                        EmulatorStatus.FAILED
                );
            }

            if (containerId != null){
                dockerEmulatorManager.stopAndRemoveContainer(containerId);
            }
        } catch (Exception e) {
            log.error("Cleanup failed. Reason {}", reason, e);
        }
    }





}
