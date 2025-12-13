package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    @Autowired private FriendService friendService;

    @GetMapping("/search")
    public List<UserDto> search(@RequestParam String q, @RequestParam Integer userId) {
        return friendService.searchUsers(q, userId);
    }
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestParam Integer senderId, @RequestParam Integer receiverId) {
        friendService.sendRequest(senderId, receiverId);
        return ResponseEntity.ok("Sent");
    }
    @GetMapping("/requests")
    public List<UserDto> getRequests(@RequestParam Integer userId) {
        return friendService.getPendingRequests(userId);
    }
    @GetMapping
    public List<UserDto> getFriends(@RequestParam Integer userId) {
        return friendService.getFriendList(userId);
    }
    @PostMapping("/requests/{id}")
    public ResponseEntity<String> respond(@PathVariable Integer id, @RequestParam boolean accept) {
        friendService.respondToRequest(id, accept);
        return ResponseEntity.ok(accept ? "Accepted" : "Declined");
    }
}