package com.example.chatroomserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chatroomserver.dto.FriendshipDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.dto.UserFriendStatsDto;
import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.FriendRequestRepository;
import com.example.chatroomserver.repository.UserRepository;

@Service
public class FriendService {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;

    @Transactional
    public void sendRequest(Integer senderId, Integer receiverId) {
        if (senderId.equals(receiverId)) throw new RuntimeException("Cannot add yourself");

        // 1. Check if YOU sent a request (or are already friends)
        friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId).ifPresent(req -> {
            if (req.getStatus() == FriendRequest.Status.ACCEPTED) {
                throw new RuntimeException("You are already friends.");
            } else if (req.getStatus() == FriendRequest.Status.PENDING) {
                throw new RuntimeException("Request already sent.");
            } else if (req.getStatus() == FriendRequest.Status.BLOCKED) {
                throw new RuntimeException("You cannot add this user.");
            }
        });

        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

        // 2. Check if THEY sent a request (or are already friends)
        friendRequestRepository.findBySenderIdAndReceiverId(receiverId, senderId).ifPresent(req -> {
            if (req.getStatus() == FriendRequest.Status.ACCEPTED) {
                throw new RuntimeException("You are already friends.");
            } else if (req.getStatus() == FriendRequest.Status.PENDING) {
                throw new RuntimeException("User already sent you a request. Check your inbox.");
            } else if (req.getStatus() == FriendRequest.Status.BLOCKED) {
                throw new RuntimeException("You cannot add this user.");
            }
        });

        // 3. Create new request
        FriendRequest req = new FriendRequest(sender, receiver);
        friendRequestRepository.save(req);
    }

    public List<UserDto> getFriendList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(req -> {
                    User friend = req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender();
                    return convertToDto(friend);
                })
                .collect(Collectors.toList());
    }

    public List<UserDto> getPendingRequests(Integer userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.Status.PENDING)
                .stream()
                .map(req -> convertToDto(req.getSender()))
                .collect(Collectors.toList());
    }

    // *** THIS IS THE FIX ***
    // Replaces the old 'respondToRequest'
    public void respondToRequestByUsers(Integer senderId, Integer receiverId, boolean accept) {
        System.out.println("Processing Friend Response: Sender=" + senderId + ", Receiver=" + receiverId + ", Accept=" + accept);

        // 1. Find request where Sender is Friend and Receiver is You
        FriendRequest req = friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElse(null);

        // 2. Inverse check (just in case)
        if (req == null) {
            req = friendRequestRepository.findBySenderIdAndReceiverId(receiverId, senderId).orElse(null);
        }

        if (req == null) {
            throw new RuntimeException("Friend Request NOT FOUND between " + senderId + " and " + receiverId);
        }

        if (accept) {
            req.setStatus(FriendRequest.Status.ACCEPTED);
            friendRequestRepository.save(req);
        } else {
            friendRequestRepository.delete(req);
        }
    }

    public void blockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        List<FriendRequest> relations = friendRequestRepository.findRelationship(blocker, target);

        if (!relations.isEmpty()) {
            for (FriendRequest rel : relations) {
                rel.setStatus(FriendRequest.Status.BLOCKED);
                rel.setSender(blocker);
                rel.setReceiver(target);
                friendRequestRepository.save(rel);
            }
        } else {
            FriendRequest block = new FriendRequest(blocker, target, FriendRequest.Status.BLOCKED);
            friendRequestRepository.save(block);
        }
    }

    public List<UserDto> getBlockedList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findBySenderAndStatus(user, FriendRequest.Status.BLOCKED)
                .stream()
                .map(req -> convertToDto(req.getReceiver()))
                .collect(Collectors.toList());
    }

    public void unblockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        List<FriendRequest> relations = friendRequestRepository.findRelationship(blocker, target);
        friendRequestRepository.deleteAll(relations);
    }

    public void unfriend(Integer userId, Integer friendId) {
        User u1 = userRepository.findById(userId).orElseThrow();
        User u2 = userRepository.findById(friendId).orElseThrow();
        List<FriendRequest> relations = friendRequestRepository.findRelationship(u1, u2);
        friendRequestRepository.deleteAll(relations);
    }

    public List<UserDto> searchUsers(String query, Integer currentUserId) {
        return userRepository.findByUsernameContainingOrFullNameContaining(query, query).stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserFriendStatsDto> getAllUserFriendStats() {
        return userRepository.findAll().stream().map(user -> {
            int friendCount = friendRequestRepository.findAllFriends(user).size();
            return new UserFriendStatsDto(
                    user.getUsername(), user.getFullName(), user.getAddress(),
                    user.getDob() != null ? user.getDob().toLocalDate() : null,
                    user.getEmail(), user.getGender() != null ? user.getGender().name() : "N/A",
                    friendCount, 0
            );
        }).collect(Collectors.toList());
    }

    private UserDto convertToDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setAddress(u.getAddress());
        dto.setStatus(u.getStatus() != null ? u.getStatus().name() : "ACTIVE");
        return dto;
    }

    public List<FriendshipDto> getFriendshipDetails(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        return friendRequestRepository.findAllFriends(user).stream()
                .map(req -> {
                    // 1. Identify the friend
                    User friend = req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender();

                    FriendshipDto dto = new FriendshipDto();

                    // 2. Set Basic Info
                    dto.setId(friend.getId());
                    dto.setUsername(friend.getUsername());
                    dto.setFullName(friend.getFullName() != null ? friend.getFullName() : ""); // Prevent null
                    String status = (friend.getStatus() != null) ? friend.getStatus().name() : "OFFLINE";
                    dto.setStatus(status);
                    String since = (req.getCreatedAt() != null) ? req.getCreatedAt().toString() : "N/A";
                    dto.setSince(since);

                    return dto;
                })
                .collect(Collectors.toList());
    }
}