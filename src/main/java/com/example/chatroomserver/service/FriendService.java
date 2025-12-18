package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.FriendshipDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.dto.UserFriendStatsDto;
import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.FriendRequestRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;

    // --- SEND REQUEST ---
    @Transactional
    public void sendRequest(Integer senderId, Integer receiverId) {
        if (senderId.equals(receiverId)) throw new RuntimeException("Cannot add yourself");

        if (friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId).isPresent()) {
            throw new RuntimeException("Request already sent");
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (friendRequestRepository.findBySenderIdAndReceiverId(receiverId, senderId).isPresent()) {
            throw new RuntimeException("User already sent you a request. Check your inbox.");
        }

        FriendRequest req = new FriendRequest(sender, receiver);
        friendRequestRepository.save(req);
    }

    // --- FRIEND LIST ---
    public List<UserDto> getFriendList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(req -> {
                    User friend = req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender();
                    return convertToDto(friend);
                })
                .collect(Collectors.toList());
    }

    // --- PENDING REQUESTS ---
    public List<UserDto> getPendingRequests(Integer userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.Status.PENDING)
                .stream()
                .map(req -> convertToDto(req.getSender()))
                .collect(Collectors.toList());
    }

    // --- ACCEPT / REJECT ---
    public void respondToRequest(Integer requestId, boolean accept) {
        FriendRequest req = friendRequestRepository.findById(requestId).orElseThrow();
        if (accept) {
            req.setStatus(FriendRequest.Status.ACCEPTED);
            friendRequestRepository.save(req);
        } else {
            friendRequestRepository.delete(req);
        }
    }

    // --- BLOCK USER ---
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

    // --- SEARCH ---
    public List<UserDto> searchUsers(String query, Integer currentUserId) {
        return userRepository.findByUsernameContainingOrFullNameContaining(query, query).stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- FIX: UPDATED ARGUMENT ORDER FOR STATS ---
    public List<UserFriendStatsDto> getAllUserFriendStats() {
        return userRepository.findAll().stream().map(user -> {
            int friendCount = friendRequestRepository.findAllFriends(user).size();

            return new UserFriendStatsDto(
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress(),                                          // 3. Address (String)
                    user.getDob() != null ? user.getDob().toLocalDate() : null, // 4. DOB (LocalDate)
                    user.getEmail(),                                            // 5. Email (String)
                    user.getGender() != null ? user.getGender().name() : "N/A", // 6. Gender (String)
                    friendCount,
                    0
            );
        }).collect(Collectors.toList());
    }

    // --- HELPER ---
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

    public List<FriendshipDto> getFriendshipDetails(Integer userId) { return new ArrayList<>(); }
}