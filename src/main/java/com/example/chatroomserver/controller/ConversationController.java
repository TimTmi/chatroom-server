package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.ConversationDto;
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

    // --- 1. ADMIN ENDPOINT (Restored) ---
    @GetMapping("/groups/all")
    public List<GroupChatDto> getAllGroups() {
        return conversationService.getAllGroups();
    }

    // --- 2. CREATE GROUP (Fixed with Group Name) ---
    @PostMapping("/group")
    public Conversation createGroup(
            @RequestParam Integer creatorId,
            @RequestParam String groupName,
            @RequestParam List<Integer> memberIds
    ) {
        return conversationService.createGroupConversation(creatorId, groupName, memberIds);
    }

    @PostMapping("/dm")
    public Conversation createDM(
            @RequestParam Integer userAId,
            @RequestParam Integer userBId
    ) {
        return conversationService.createDirectConversation(userAId, userBId);
    }

    @GetMapping
    public List<ConversationDto> listUserConversations(@RequestParam Integer userId) {
        return conversationService.getUserConversationsDto(userId);
    }

    @DeleteMapping("/{conversationId}")
    public void deleteConversation(
            @PathVariable Integer conversationId,
            @RequestParam Integer userId
    ) {
        System.out.println(conversationId);
        conversationService.deleteConversation(conversationId, userId);
    }

}