package com.example.chatroomserver.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUsersService {

    private final Map<String, Long> onlineUsers = new ConcurrentHashMap<>();

    // mark user online (heartbeat or connect)
    public void setOnline(String userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
    }

    // mark user offline (disconnect)
    public void setOffline(String userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    public Map<String, Long> getOnlineUsers() {
        return onlineUsers;
    }

    // remove users who haven't pinged within the timeout
    public void cleanupStaleUsers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        onlineUsers.entrySet().removeIf(entry -> now - entry.getValue() > timeoutMillis);
    }
}
