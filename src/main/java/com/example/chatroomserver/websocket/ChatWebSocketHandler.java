package com.example.chatroomserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // userId -> WebSocketSession
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // sessionId -> userId (reverse lookup)
    private final ConcurrentHashMap<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);

        sessions.put(userId, session);
        sessionIdToUserId.put(session.getId(), userId);

        // Create online snapshot using Jackson
        ObjectMapper mapper = new ObjectMapper();
        List<Long> onlineUserIds = sessions.keySet().stream()
                .filter(id -> !id.equals(userId))
                .toList();
        String onlineJson = mapper.writeValueAsString(onlineUserIds);

        session.sendMessage(new TextMessage("{\"type\":\"ONLINE_SNAPSHOT\",\"users\":" + onlineJson + "}"));

        // Broadcast new user status
        broadcastStatus(userId, true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = sessionIdToUserId.remove(session.getId());
        if (userId != null) {
            sessions.remove(userId);
            broadcastStatus(userId, false);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // optional: handle chat messages here
    }

    private void broadcastStatus(Long userId, boolean online) {
        String msg = String.format(
                "{\"type\":\"STATUS\",\"userId\":%d,\"online\":%b}",
                userId, online
        );

        sessions.values().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(msg));
            } catch (Exception ignored) {}
        });
    }

    private Long extractUserId(WebSocketSession session) {
        // Example: ws://localhost:8080/ws?userId=12
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return Long.parseLong(query.split("=")[1]);
        }
        return null;
    }
}
