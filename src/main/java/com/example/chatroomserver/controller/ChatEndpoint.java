package com.example.chatroomserver.controller;

import com.example.chatroomserver.service.OnlineUsersService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ServerEndpoint("/chat")
public class ChatEndpoint {

    private static OnlineUsersService onlineUsersService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Spring injects the service statically for ServerEndpoint
    @Autowired
    public void setOnlineUsersService(OnlineUsersService service) {
        ChatEndpoint.onlineUsersService = service;
    }

    @OnOpen
    public void onOpen(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        onlineUsersService.setOnline(userId);
        System.out.println(userId + " connected");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String userId = (String) session.getUserProperties().get("userId");

        try {
            JsonNode json = objectMapper.readTree(message);
            String type = json.get("type").asText();

            if ("heartbeat".equals(type)) {
                onlineUsersService.setOnline(userId);
            } else if ("chat".equals(type)) {
                String chatMessage = json.get("message").asText();
                System.out.println(userId + ": " + chatMessage);
                onlineUsersService.setOnline(userId); // optional heartbeat on chat
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        onlineUsersService.setOffline(userId);
        System.out.println(userId + " disconnected");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
