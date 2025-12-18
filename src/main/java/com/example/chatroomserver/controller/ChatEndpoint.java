package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.MessageDto;
import com.example.chatroomserver.service.MessageService;
import com.example.chatroomserver.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/chat/{conversationId}")
public class ChatEndpoint {

    private static MessageService messageServiceStatic;
    private static UserService userServiceStatic;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Track all sessions per conversation
    private static final Map<Integer, Set<Session>> sessionsPerConversation = new ConcurrentHashMap<>();

    private Session session;
    private Integer conversationId;
    private Integer userId;

    // Spring injection hack for @ServerEndpoint
    @Autowired
    public void setServices(MessageService messageService, UserService userService) {
        ChatEndpoint.messageServiceStatic = messageService;
        ChatEndpoint.userServiceStatic = userService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("conversationId") Integer conversationId) {
        this.session = session;
        this.conversationId = conversationId;
        sessionsPerConversation
                .computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        System.out.println("New WebSocket session " + session.getId() + " for conversation " + conversationId);
    }

    @OnMessage
    public void onMessage(String messageJson) {
        try {
            var node = objectMapper.readTree(messageJson);

            // Expect { "userId":123, "message":"Hello!" }
            int senderId = node.get("userId").asInt();
            String text = node.get("message").asText();

            // Persist to DB
            MessageDto dto = messageServiceStatic.sendMessage(conversationId, senderId, text);

            // Broadcast to all sessions of this conversation
            String broadcastJson = objectMapper.writeValueAsString(dto);
            sessionsPerConversation.getOrDefault(conversationId, Set.of()).forEach(s -> {
                try { s.getBasicRemote().sendText(broadcastJson); }
                catch (Exception e) { e.printStackTrace(); }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
        if (conversationId != null && session != null) {
            sessionsPerConversation.getOrDefault(conversationId, Set.of()).remove(session);
        }
        System.out.println("WebSocket session closed: " + session.getId());
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }
}
