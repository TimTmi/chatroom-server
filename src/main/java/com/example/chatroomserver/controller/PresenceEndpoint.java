package com.example.chatroomserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/presence")
public class PresenceEndpoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
                System.out.println("Heartbeat from " + username);
                // mark user as online
                // Optional: send ack
                session.getBasicRemote().sendText("{\"type\":\"heartbeat_ack\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Presence connection closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
