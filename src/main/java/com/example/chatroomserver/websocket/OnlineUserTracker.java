package com.example.chatroomserver.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserTracker {

    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();

    public void add(Long userId, String sessionId) {
        userSessions.put(userId, sessionId);
    }

    public void removeBySession(String sessionId) {
        userSessions.entrySet()
                .removeIf(e -> e.getValue().equals(sessionId));
    }

    public boolean isOnline(Long userId) {
        return userSessions.containsKey(userId);
    }

    public Set<Long> getOnlineUserIds() {
        return userSessions.keySet();
    }
}
