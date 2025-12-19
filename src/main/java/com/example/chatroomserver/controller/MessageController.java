package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.MessageDto;
import com.example.chatroomserver.service.MessageSearchService;
import com.example.chatroomserver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    MessageSearchService messageSearchService;

    @PostMapping
    public MessageDto send(
            @RequestParam Integer conversationId,
            @RequestParam Integer senderId,
            @RequestParam String content
    ) {
        return messageService.sendMessage(conversationId, senderId, content);
    }

    @GetMapping("/{conversationId}")
    public List<MessageDto> list(
            @PathVariable Integer conversationId,
            @RequestParam Integer userId
    ) {
        return messageService.getMessages(conversationId, userId);
    }

    @GetMapping("/search/all")
    public List<MessageDto> searchAllMessages(
            @RequestParam Integer userId,
            @RequestParam String query
    ) {
        return messageSearchService.searchAllMessages(userId, query);
    }

}
