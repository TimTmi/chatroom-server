package com.example.chatroomserver.config;

import com.example.chatroomserver.service.OnlineUsersService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OnlineUsersCleanup {

    private final OnlineUsersService onlineUsersService;

    public OnlineUsersCleanup(OnlineUsersService onlineUsersService) {
        this.onlineUsersService = onlineUsersService;
    }

    // runs every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void removeStaleUsers() {
        onlineUsersService.cleanupStaleUsers(15000); // 15s timeout
    }
}
