package com.projects.applivo.emulator;

import com.projects.applivo.entity.EmulatorInstance;
import com.projects.applivo.entity.EmulatorStatus;
import com.projects.applivo.entity.SessionStatus;
import com.projects.applivo.repository.EmulatorInstanceRepository;
import com.projects.applivo.repository.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmulatorPersistenceService {

    private final SessionRepository sessionRepository;
    private final EmulatorInstanceRepository emulatorRepository;

    @Transactional
    public void updateSessionStatus(Long sessionId, SessionStatus status) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setSessionStatus(status);
            if (status == SessionStatus.ENDED || status == SessionStatus.FAILED) {
                s.setEndedAt(Instant.now());
            }
            sessionRepository.save(s);
        });
    }

    @Transactional
    public void updateSessionContainerId(Long sessionId, String containerId) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setEmulatorContainerId(containerId);
            sessionRepository.save(s);
        });
    }

    @Transactional
    public EmulatorInstance saveEmulatorInstance(EmulatorInstance emulator) {
        return emulatorRepository.save(emulator);
    }

    @Transactional
    public void updateEmulatorStatus(Long emulatorId, EmulatorStatus status) {
        emulatorRepository.findById(emulatorId).ifPresent(e -> {
            e.setStatus(status);
            emulatorRepository.save(e);
        });
    }

    @Transactional
    public void updateEmulatorResolution(Long emulatorId, int width, int height) {
        emulatorRepository.findById(emulatorId).ifPresent(e -> {
            e.setScreenWidth(width);
            e.setScreenHeight(height);
            emulatorRepository.save(e);
        });
    }

    @Transactional
    public void updateSessionFailure(Long sessionId, String reason) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setSessionStatus(SessionStatus.FAILED);
            s.setFailureReason(reason);
            s.setEndedAt(Instant.now());
            sessionRepository.save(s);
        });
    }

}
