package com.projects.applivo.websocket;

import com.projects.applivo.emulator.EmulatorService;
import com.projects.applivo.emulator.ScreenshotStreamingService;
import com.projects.applivo.entity.Session;
import com.projects.applivo.entity.SessionStatus;
import com.projects.applivo.entity.User;
import com.projects.applivo.exception.ResourceNotFoundException;
import com.projects.applivo.repository.SessionRepository;
import com.projects.applivo.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DemoSessionHandler {

    private final EmulatorService emulatorService;

    private final SessionService sessionService;

    private final SessionRepository sessionRepository;

    private final ScreenshotStreamingService streamingService;

    @MessageMapping("/demo/{sessionId}/interact")
    public void handleInteraction(
            @Payload InteractionMessage message,
            @DestinationVariable Long sessionId,
            Principal principal ){

        Session session = getActiveSession(sessionId, principal);
        emulatorService.handleInput(session.getEmulatorContainerId(), message);
        sessionService.updateLastActivity(sessionId);

    }

    @MessageMapping("/demo/{sessionId}/start-stream")
    public void startStream(@DestinationVariable Long sessionId,
                            Principal principal,
                            SimpMessageHeaderAccessor headerAccessor){

        Session session = getActiveSession(sessionId, principal);

        WebSocketSession webSocketSession = (WebSocketSession) headerAccessor.getSessionAttributes().get("wsSession");

        if (webSocketSession == null){
            log.warn("WebSocket session not found for session {}", sessionId);
            return;
        }

        streamingService.startStreaming(
                sessionId,
                session.getEmulatorContainerId(),
                webSocketSession
        );

    }


    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String simpSessionId = accessor.getSessionId();

        log.info("WebSocket disconnect for {}", simpSessionId);
    }

    private Session getActiveSession(Long sessionId, Principal principal){
        User user = (User) ((Authentication)principal).getPrincipal();
        return sessionRepository.findByIdAndUser(sessionId, user)
                .filter(s -> s.getSessionStatus() == SessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found!"));
    }

}
