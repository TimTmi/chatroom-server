package com.example.chatroomserver.websocket;

import com.example.chatroomserver.dto.UserStatusDto;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final OnlineUserTracker tracker;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(
            OnlineUserTracker tracker,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.tracker = tracker;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // You must pass userId from client when connecting
        String userIdHeader = accessor.getFirstNativeHeader("userId");
        if (userIdHeader == null) return;

        Long userId = Long.parseLong(userIdHeader);
        String sessionId = accessor.getSessionId();

        tracker.add(userId, sessionId);

        messagingTemplate.convertAndSend(
                "/topic/user-status",
                new UserStatusDto(userId, true)
        );
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        // find user by session
        tracker.getOnlineUserIds().forEach(userId -> {
            // brute-force but fine for small scale
            tracker.removeBySession(sessionId);
            messagingTemplate.convertAndSend(
                    "/topic/user-status",
                    new UserStatusDto(userId, false)
            );
        });
    }
}

