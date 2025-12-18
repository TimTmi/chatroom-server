package com.example.chatroomserver.controller;

import com.example.chatroomserver.service.OnlineUsersService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final Map<Session, String> sessionUserMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New connection: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonNode json = objectMapper.readTree(message);
            if (json == null || json.isNull()) {
                System.out.println("Received empty or null JSON: " + message);
                return;
            }

            JsonNode typeNode = json.get("type");
            if (typeNode == null || typeNode.isNull()) {
                System.out.println("Received JSON without 'type': " + message);
                return;
            }

            String type = typeNode.asText();

            switch (type) {
                case "login" -> {
                    JsonNode usernameNode = json.get("username");
                    JsonNode passwordNode = json.get("password");

                    if (usernameNode == null || passwordNode == null) {
                        send(session, "login_failed_missing_fields");
                        return;
                    }

                    String username = usernameNode.asText();
                    String password = passwordNode.asText();

                    if (authenticate(username, password)) {
                        sessionUserMap.put(session, username);
                        onlineUsersService.setOnline(username);
                        send(session, "login_success");
                        System.out.println(username + " logged in");
                    } else {
                        send(session, "login_failed");
                    }
                }

                case "heartbeat" -> {
                    String userId = sessionUserMap.get(session);
                    if (userId != null) {
                        onlineUsersService.setOnline(userId);
                        System.out.println("Heartbeat received from " + userId + " at " + System.currentTimeMillis());
                    } else {
                        System.out.println("Heartbeat from unknown session: " + session.getId());
                    }
                }

                case "chat" -> {
                    String userId = sessionUserMap.get(session);
                    if (userId != null) {
                        JsonNode textNode = json.get("message");
                        if (textNode != null && !textNode.isNull()) {
                            String text = textNode.asText();
                            System.out.println(userId + ": " + text);
                            onlineUsersService.setOnline(userId);
                        } else {
                            System.out.println("Chat message missing 'message' field from " + userId);
                        }
                    } else {
                        System.out.println("Chat from unknown session: " + session.getId());
                    }
                }

                default -> System.out.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.out.println("Failed to parse message: " + message);
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String userId = sessionUserMap.remove(session);
        if (userId != null) {
            onlineUsersService.setOffline(userId);
            System.out.println(userId + " disconnected");
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private boolean authenticate(String username, String password) {
        // Replace this with a real DB lookup
        return "user".equals(username) && "pass".equals(password);
    }

    private void send(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
