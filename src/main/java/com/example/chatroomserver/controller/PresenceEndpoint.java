package com.example.chatroomserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/presence")
public class PresenceEndpoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Session> onlineUsers = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Presence connection opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ObjectNode json = (ObjectNode) objectMapper.readTree(message);
            if ("heartbeat".equals(json.get("type").asText())) {
                String username = json.get("username").asText();
                onlineUsers.put(username, session); // mark online
                broadcastOnlineUsers();
                session.getBasicRemote().sendText("{\"type\":\"heartbeat_ack\"}");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @OnClose
    public void onClose(Session session) {
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().equals(session));
        broadcastOnlineUsers();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastOnlineUsers() {
        ObjectNode update = objectMapper.createObjectNode();
        update.put("type", "online_users");
        update.putPOJO("users", onlineUsers.keySet());
        onlineUsers.values().forEach(s -> {
            try { s.getBasicRemote().sendText(update.toString()); }
            catch (Exception e) { e.printStackTrace(); }
        });
    }
}
