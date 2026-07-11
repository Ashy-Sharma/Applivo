package com.projects.applivo.emulator;

import com.projects.applivo.websocket.ScreenshotMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScreenshotStreamingService {

    private final EmulatorService emulatorService;
    private final ObjectMapper objectMapper;

    private final Map<Long, ScheduledFuture<?>> activeStreams = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void startStreaming(Long sessionId, String containerId, WebSocketSession webSocketSession){

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!webSocketSession.isOpen()){
                    stopStreaming(sessionId);
                    return;
                }

                byte[] screenshot = emulatorService.captureScreenshot(containerId);
                String base64 = Base64.getEncoder().encodeToString(screenshot);

                ScreenshotMessage message = ScreenshotMessage.builder()
                        .data(base64)
                        .timestamp(Instant.now())
                        .build();

                webSocketSession.sendMessage(
                        new TextMessage(objectMapper.writeValueAsString(message))
                );
            } catch (Exception e) {
                log.error("Screenshot streaming error for session {} : {}", sessionId, e.getMessage());
                stopStreaming(sessionId);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        activeStreams.put(sessionId, future);
        log.info("Started screenshot streaming for session {}", sessionId);

    }

    public void stopStreaming(Long sessionId){

        ScheduledFuture<?> future = activeStreams.remove(sessionId);

        if (future != null){
            future.cancel(true);
            log.info("Stopped screenshot streaming for session {}", sessionId);
        }

    }

    public boolean isStreaming(Long sessionId){
        ScheduledFuture<?> future = activeStreams.get(sessionId);
        return future != null && !future.isDone();
    }

    

}
