package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.FriendshipDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.dto.UserFriendStatsDto;
import com.example.chatroomserver.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String q, @RequestParam Integer userId) {
        return friendService.searchUsers(q, userId);
    }

    @PostMapping("/requests")
    public ResponseEntity<Void> sendRequest(@RequestParam Integer senderId, @RequestParam Integer receiverId) {
        friendService.sendRequest(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests")
    public List<UserDto> getPendingRequests(@RequestParam Integer userId) {
        return friendService.getPendingRequests(userId);
    }

    // *** THIS IS THE FIX ***
    @PostMapping("/requests/{senderId}/response")
    public ResponseEntity<Void> respondToRequest(
            @PathVariable Integer senderId,      // Friend's ID
            @RequestParam Integer receiverId,    // Your ID
            @RequestParam boolean accept
    ) {
        // Use the NEW method that finds by users
        friendService.respondToRequestByUsers(senderId, receiverId, accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<UserDto> getFriends(@RequestParam Integer userId) {
        return friendService.getFriendList(userId);
    }

    @DeleteMapping
    public ResponseEntity<Void> unfriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        friendService.unfriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block")
    public ResponseEntity<Void> blockUser(@RequestParam Integer userId, @RequestParam Integer targetId) {
        friendService.blockUser(userId, targetId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/block")
    public ResponseEntity<Void> unblockUser(@RequestParam Integer userId, @RequestParam Integer targetId) {
        friendService.unblockUser(userId, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/blocked")
    public List<UserDto> getBlockedUsers(@RequestParam Integer userId) {
        return friendService.getBlockedList(userId);
    }

    @GetMapping("/details")
    public List<FriendshipDto> getFriendshipDetails(@RequestParam Integer userId) {
        return friendService.getFriendshipDetails(userId);
    }

    @GetMapping("/admin/stats")
    public List<UserFriendStatsDto> getAllUserStats() {
        return friendService.getAllUserFriendStats();
    }
}