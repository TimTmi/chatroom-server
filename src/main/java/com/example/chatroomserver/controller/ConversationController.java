package com.example.chatroomserver.controller;

import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * Create a DM
     */
    @PostMapping("/dm")
    public Conversation createDM(
            @RequestParam Integer userAId,
            @RequestParam Integer userBId
    ) {
        return conversationService.createDirectConversation(userAId, userBId);
    }

    /**
     * Create a group conversation
     */
    @PostMapping("/group")
    public Conversation createGroup(
            @RequestParam Integer creatorId,
            @RequestParam List<Integer> memberIds
    ) {
        return conversationService.createGroupConversation(creatorId, memberIds);
    }

    /**
     * List conversations for a user
     */
    @GetMapping
    public List<Conversation> listUserConversations(
            @RequestParam Integer userId
    ) {
        return conversationService.getUserConversations(userId);
    }
}
