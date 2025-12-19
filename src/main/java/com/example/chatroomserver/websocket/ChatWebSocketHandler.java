package com.example.chatroomserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        System.out.println("WebSocket connected: user " + userId + ", session " + session.getId());

        sessions.put(userId, session);
        sessionIdToUserId.put(session.getId(), userId);

        ObjectMapper mapper = new ObjectMapper();
        List<Long> onlineUserIds = sessions.keySet().stream()
                .filter(id -> !id.equals(userId))
                .toList();
        String onlineJson = mapper.writeValueAsString(onlineUserIds);

        session.sendMessage(new TextMessage("{\"type\":\"ONLINE_SNAPSHOT\",\"users\":" + onlineJson + "}"));

        broadcastStatus(userId, true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = sessionIdToUserId.remove(session.getId());
        System.out.println("WebSocket closed: session " + session.getId() + ", user " + userId + ", reason: " + status);
        if (userId != null) {
            sessions.remove(userId);
            broadcastStatus(userId, false);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(payload, Map.class);
            String type = (String) map.get("type");

            if ("LOGOUT".equals(type)) {
                System.out.println("Received logout from session " + session.getId());
                afterConnectionClosed(session, CloseStatus.NORMAL);
            }
            else if ("MESSAGE".equals(type)) {
                Long senderId = sessionIdToUserId.get(session.getId());
                Long conversationId = Long.parseLong(map.get("conversationId").toString());
                String content = (String) map.get("content");
                String senderName = (String) map.get("senderName");

                String msg = String.format(
                        "{\"type\":\"MESSAGE\",\"conversationId\":%d,\"senderId\":%d,\"senderName\":\"%s\",\"content\":\"%s\"}",
                        conversationId, senderId, senderName, content.replace("\"", "\\\"")
                );

                sessions.values().forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(msg));
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastStatus(Long userId, boolean online) {
        String msg = String.format(
                "{\"type\":\"STATUS\",\"userId\":%d,\"online\":%b}",
                userId, online
        );

        sessions.values().forEach(s -> {
            try { s.sendMessage(new TextMessage(msg)); }
            catch (Exception ignored) {}
        });
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return Long.parseLong(query.split("=")[1]);
        }
        return null;
    }
}
