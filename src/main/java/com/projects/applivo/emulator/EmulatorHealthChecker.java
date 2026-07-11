package com.projects.applivo.emulator;

import com.projects.applivo.entity.EmulatorInstance;
import com.projects.applivo.entity.EmulatorStatus;
import com.projects.applivo.entity.Session;
import com.projects.applivo.entity.SessionStatus;
import com.projects.applivo.repository.EmulatorInstanceRepository;
import com.projects.applivo.repository.SessionRepository;
import com.projects.applivo.service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmulatorHealthChecker {

    private final EmulatorInstanceRepository emulatorInstanceRepository;

    private final SessionRepository sessionRepository;

    private final DockerEmulatorManager dockerEmulatorManager;

    private final ScreenshotStreamingService streamingService;

    private final SessionService sessionService;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void checkContainerHealth(){

        List<EmulatorInstance> active = emulatorInstanceRepository.findByStatusIn(List.of(EmulatorStatus.STARTING, EmulatorStatus.RUNNING));

        for (EmulatorInstance emulatorInstance : active){
            if (!dockerEmulatorManager.isContainerRunning(emulatorInstance.getContainerId())){

                log.warn("Container {} is dead. Cleaning up.", emulatorInstance.getContainerId());

                emulatorInstance.setStatus(EmulatorStatus.FAILED);
                emulatorInstanceRepository.save(emulatorInstance);

                if (emulatorInstance.getSession() != null){
                    Long sessionId = emulatorInstance.getSession().getId();
                    streamingService.stopStreaming(sessionId);

                    emulatorInstance.getSession().setSessionStatus(SessionStatus.TIMED_OUT);
                    sessionRepository.save(emulatorInstance.getSession());

                }

            }
        }

    }


    @Scheduled(fixedDelay = 60000)
    public void cleanupIdleSessions(){
        List<Session> activeSessions = sessionRepository.findBySessionStatus(SessionStatus.ACTIVE);

        Instant idleThreshold = Instant.now().minus(5, ChronoUnit.MINUTES);

        for (Session session : activeSessions){
            if (session.getLastActivityAt() != null && session.getLastActivityAt().isBefore(idleThreshold)){
                log.info("Session {} is idle. Cleaning up.", session.getId());
                streamingService.stopStreaming(session.getId());

                try {
                    sessionService.endSession(session.getId(), session.getUser());
                } catch (Exception e) {
                    log.error("Failed to cleanup session {}", session.getId(), e);
                }
            }
        }
    }

}
