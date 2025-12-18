package com.example.chatroomserver.controller;

import com.example.chatroomserver.service.OnlineUsersService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/chat")
public class ChatEndpoint {

    private static OnlineUsersService onlineUsersService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setOnlineUsersService(OnlineUsersService service) {
        ChatEndpoint.onlineUsersService = service;
    }

    // Maps sessions to usernames (optional, for debugging/logging)
    private final Map<Session, String> sessionUserMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New connection: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonNode json = objectMapper.readTree(message);
            if (!json.has("type")) {
                System.out.println("Unknown message: " + message);
                return;
            }

            String type = json.get("type").asText();
            String username = json.has("username") ? json.get("username").asText() : null;

            switch (type) {
                case "heartbeat" -> handleHeartbeat(session, username);
                case "chat" -> handleChat(session, username, json);
                default -> System.out.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleHeartbeat(Session session, String username) {
        if (username != null) {
            onlineUsersService.setOnline(username);
            sessionUserMap.put(session, username); // track for optional debugging
            System.out.println("Heartbeat received from " + username + " at " + System.currentTimeMillis());
            send(session, "heartbeat_ack");
        } else {
            System.out.println("Heartbeat from unknown session: " + session.getId());
        }
    }

    private void handleChat(Session session, String username, JsonNode json) {
        if (username == null) {
            System.out.println("Chat from unknown session: " + session.getId());
            return;
        }

        String text = json.has("message") ? json.get("message").asText() : null;
        if (text != null) {
            System.out.println(username + ": " + text);
            onlineUsersService.setOnline(username); // optional, keep user online
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = sessionUserMap.remove(session);
        if (username != null) {
            onlineUsersService.setOffline(username);
            System.out.println(username + " disconnected");
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void send(Session session, String type) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("type", type);
            session.getBasicRemote().sendText(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
