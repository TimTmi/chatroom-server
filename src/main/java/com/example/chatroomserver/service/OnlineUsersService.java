package com.example.chatroomserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OnlineUsersService {

    private final Map<String, Long> onlineUsers = new ConcurrentHashMap<>();

    public void setOnline(String userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
    }

    public void setOffline(String userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    public Map<String, Long> getOnlineUsers() {
        return onlineUsers;
    }

    public void cleanupStaleUsers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        onlineUsers.entrySet().removeIf(entry -> now - entry.getValue() > timeoutMillis);
    }
}
