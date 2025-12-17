package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.GroupChatDto;
import com.example.chatroomserver.service.GroupChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupChatController {

    @Autowired private GroupChatService groupChatService;

    @PostMapping
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> payload) {
        try {
            Integer adminId = Integer.parseInt(payload.get("adminId").toString());
            String name = (String) payload.get("name");
            List<Integer> memberIds = (List<Integer>) payload.get("memberIds");

            groupChatService.createGroup(adminId, name, memberIds);
            return ResponseEntity.ok("Group created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @GetMapping
    public List<GroupChatDto> getAllGroups() {
        return groupChatService.getAllGroups();
    }
}