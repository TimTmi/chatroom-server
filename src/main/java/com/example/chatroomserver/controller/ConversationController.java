package com.example.chatroomserver.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatroomserver.dto.ConversationDto;
import com.example.chatroomserver.dto.GroupChatDto;
import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.service.ConversationService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    // --- 1. ADMIN ENDPOINT ---
    @GetMapping("/groups/all")
    public List<GroupChatDto> getAllGroups() {
        return conversationService.getAllGroups();
    }

    // --- 2. CREATE GROUP (Merged: Uses your version with adminIds) ---
    @PostMapping("/group")
    public Conversation createGroup(
            @RequestParam Integer creatorId,
            @RequestParam String groupName,
            @RequestParam List<Integer> memberIds,
            @RequestParam(required = false) List<Integer> adminIds 
    ) {
        return conversationService.createGroupConversation(creatorId, groupName, memberIds, adminIds);
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

    // --- 3. DELETE CONVERSATION (Merged: From your friend's file) ---
    @DeleteMapping("/{conversationId}")
    public void deleteConversation(
            @PathVariable Integer conversationId,
            @RequestParam Integer userId
    ) {
        conversationService.deleteConversation(conversationId, userId);
    }

    // --- 3. UPDATE GROUP (New Endpoint) ---
    @PutMapping("/group/{conversationId}")
    public void updateGroup(
            @PathVariable Integer conversationId,
            @RequestParam String groupName,
            @RequestParam List<Integer> memberIds,
            @RequestParam(required = false) List<Integer> adminIds
    ) {
        conversationService.updateGroupConversation(conversationId, groupName, memberIds, adminIds);
    }
}