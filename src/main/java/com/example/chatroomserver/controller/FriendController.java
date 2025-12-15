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

    // --- SEARCH & REQUESTS ---
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

    @PostMapping("/requests/{id}")
    public ResponseEntity<String> respond(@PathVariable Integer id, @RequestParam boolean accept) {
        friendService.respondToRequest(id, accept);
        return ResponseEntity.ok(accept ? "Accepted" : "Declined");
    }

    @GetMapping
    public List<UserDto> getFriends(@RequestParam Integer userId) {
        return friendService.getFriendList(userId);
    }

    // --- NEW ENDPOINTS (THESE WERE MISSING) ---

    @PostMapping("/unfriend")
    public ResponseEntity<String> unfriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        friendService.unfriend(userId, friendId);
        return ResponseEntity.ok("Unfriended");
    }

    @PostMapping("/block")
    public ResponseEntity<String> block(@RequestParam Integer userId, @RequestParam Integer targetId) {
        friendService.blockUser(userId, targetId);
        return ResponseEntity.ok("Blocked");
    }

    @PostMapping("/unblock")
    public ResponseEntity<String> unblock(@RequestParam Integer userId, @RequestParam Integer targetId) {
        friendService.unblockUser(userId, targetId);
        return ResponseEntity.ok("Unblocked");
    }

    @GetMapping("/blocked")
    public List<UserDto> getBlocked(@RequestParam Integer userId) {
        return friendService.getBlockedList(userId);
    }

    @GetMapping("/details")
    public List<com.example.chatroomserver.dto.FriendshipDto> getFriendshipDetails(@RequestParam Integer userId) {
        return friendService.getFriendshipDetails(userId);
    }
}