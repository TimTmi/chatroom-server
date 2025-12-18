package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.GroupChatDto;
import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    // --- NEW: Endpoint for Admin Group View ---
    @GetMapping("/groups/all")
    public List<GroupChatDto> getAllGroups() {
        return conversationService.getAllGroups();
    }

    // --- Existing Endpoints ---

    @PostMapping("/dm")
    public Conversation createDM(@RequestParam Integer userAId, @RequestParam Integer userBId) {
        return conversationService.createDirectConversation(userAId, userBId);
    }

    @PostMapping("/group")
    public Conversation createGroup(
            @RequestParam Integer creatorId,
            @RequestParam String groupName,
            @RequestParam List<Integer> memberIds
    ) {
        return conversationService.createGroupConversation(creatorId, groupName, memberIds);
    }

    @GetMapping
    public List<Conversation> listUserConversations(@RequestParam Integer userId) {
        return conversationService.getUserConversations(userId);
    }
}