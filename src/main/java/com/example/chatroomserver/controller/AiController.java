package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.AiRequest;
import com.example.chatroomserver.dto.AiResponse;
import com.example.chatroomserver.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggest")
    public AiResponse suggest(@RequestBody AiRequest request) {
        return new AiResponse(aiService.suggestReply(request.lastMessage()));
    }
}
