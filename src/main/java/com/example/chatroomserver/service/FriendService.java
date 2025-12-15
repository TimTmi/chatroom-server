package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.FriendRequestRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {
    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;

    public List<UserDto> searchUsers(String keyword, Integer currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        List<User> users = userRepository.findByUsernameContainingOrFullNameContaining(keyword, keyword);
        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> !friendRequestRepository.existsByUsers(currentUser, u))
                .filter(u -> !friendRequestRepository.existsBySenderAndReceiverAndStatus(u, currentUser, FriendRequest.Status.BLOCKED))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void sendRequest(Integer senderId, Integer receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        boolean isBlocked = friendRequestRepository.existsBySenderAndReceiverAndStatus(receiver, sender, FriendRequest.Status.BLOCKED);
        if (isBlocked) throw new RuntimeException("Blocked");

        if (!friendRequestRepository.existsByUsers(sender, receiver)) {
            friendRequestRepository.save(new FriendRequest(sender, receiver, FriendRequest.Status.PENDING));
        }
    }

    // --- SAFETY UPDATE: Delete ALL existing relationships before blocking/unfriending ---
    public void unfriend(Integer userId, Integer friendId) {
        User u1 = userRepository.findById(userId).orElseThrow();
        User u2 = userRepository.findById(friendId).orElseThrow();
        List<FriendRequest> rels = friendRequestRepository.findRelationship(u1, u2);
        friendRequestRepository.deleteAll(rels); // Delete ALL found rows
    }

    public void blockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();

        // 1. Delete ANY existing relationship (Friend, Request, etc.)
        List<FriendRequest> rels = friendRequestRepository.findRelationship(blocker, target);
        friendRequestRepository.deleteAll(rels);

        // 2. Add Block
        friendRequestRepository.save(new FriendRequest(blocker, target, FriendRequest.Status.BLOCKED));
    }

    public void unblockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();

        List<FriendRequest> rels = friendRequestRepository.findRelationship(blocker, target);
        for (FriendRequest r : rels) {
            // Only delete the BLOCK row I created
            if (r.getStatus() == FriendRequest.Status.BLOCKED && r.getSender().getId().equals(blockerId)) {
                friendRequestRepository.delete(r);
            }
        }
    }

    public List<UserDto> getPendingRequests(Integer userId) {
        User receiver = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING)
                .stream().map(req -> {
                    UserDto dto = convertToDto(req.getSender());
                    dto.setId(req.getId()); // Request ID
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<UserDto> getFriendList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(f -> convertToDto(f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender()))
                .collect(Collectors.toList());
    }

    public List<UserDto> getBlockedList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findBySenderAndStatus(user, FriendRequest.Status.BLOCKED).stream()
                .map(req -> convertToDto(req.getReceiver()))
                .collect(Collectors.toList());
    }

    public void respondToRequest(Integer requestId, boolean accept) {
        FriendRequest req = friendRequestRepository.findById(requestId).orElseThrow();
        if (accept) {
            req.setStatus(FriendRequest.Status.ACCEPTED);
            friendRequestRepository.save(req);
        } else {
            friendRequestRepository.delete(req);
        }
    }

    private UserDto convertToDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        return dto;
    }

    public List<com.example.chatroomserver.dto.FriendshipDto> getFriendshipDetails(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // Reuse the existing repository method that finds accepted requests
        return friendRequestRepository.findAllFriends(user).stream()
                .map(req -> {
                    // Determine which user is the "friend"
                    User friend = req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender();

                    return new com.example.chatroomserver.dto.FriendshipDto(
                            friend.getUsername(),
                            req.getStatus().toString(),
                            req.getCreatedAt().toLocalDate().toString() // Convert timestamp to Date string
                    );
                }).collect(Collectors.toList());
    }
}