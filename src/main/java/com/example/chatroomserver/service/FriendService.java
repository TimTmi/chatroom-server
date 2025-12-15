package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.dto.FriendshipDto;
import com.example.chatroomserver.dto.UserFriendStatsDto;
import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.FriendRequestRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;

    // --- SEARCH ---
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

    // --- ACTIONS ---

    public void unfriend(Integer userId, Integer friendId) {
        User u1 = userRepository.findById(userId).orElseThrow();
        User u2 = userRepository.findById(friendId).orElseThrow();
        List<FriendRequest> rels = friendRequestRepository.findRelationship(u1, u2);
        friendRequestRepository.deleteAll(rels);
    }

    public void blockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        List<FriendRequest> existing = friendRequestRepository.findRelationship(blocker, target);
        friendRequestRepository.deleteAll(existing);
        friendRequestRepository.save(new FriendRequest(blocker, target, FriendRequest.Status.BLOCKED));
    }

    public void unblockUser(Integer blockerId, Integer targetId) {
        User blocker = userRepository.findById(blockerId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        List<FriendRequest> rels = friendRequestRepository.findRelationship(blocker, target);
        for (FriendRequest r : rels) {
            if (r.getStatus() == FriendRequest.Status.BLOCKED && r.getSender().getId().equals(blockerId)) {
                friendRequestRepository.delete(r);
            }
        }
    }

    public void sendRequest(Integer senderId, Integer receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();
        boolean isBlocked = friendRequestRepository.existsBySenderAndReceiverAndStatus(receiver, sender, FriendRequest.Status.BLOCKED);
        if (isBlocked) throw new RuntimeException("You cannot send a request to this user.");
        if (friendRequestRepository.existsByUsers(sender, receiver)) throw new RuntimeException("Request already exists.");
        friendRequestRepository.save(new FriendRequest(sender, receiver, FriendRequest.Status.PENDING));
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

    // --- GET LISTS ---

    public List<UserDto> getFriendList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(f -> convertToDto(f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender()))
                .collect(Collectors.toList());
    }

    public List<UserDto> getBlockedList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findBySenderAndStatus(user, FriendRequest.Status.BLOCKED).stream()
                .map(f -> convertToDto(f.getReceiver()))
                .collect(Collectors.toList());
    }

    public List<UserDto> getPendingRequests(Integer userId) {
        User receiver = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING)
                .stream().map(req -> {
                    UserDto dto = convertToDto(req.getSender());
                    dto.setId(req.getId());
                    return dto;
                }).collect(Collectors.toList());
    }

    // --- ADMIN FEATURES ---

    public List<FriendshipDto> getFriendshipDetails(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(req -> {
                    User friend = req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender();
                    return new FriendshipDto(
                            friend.getUsername(),
                            req.getStatus().toString(),
                            req.getCreatedAt().toLocalDate().toString()
                    );
                }).collect(Collectors.toList());
    }

    public List<UserFriendStatsDto> getAllUserFriendStats() {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream().map(user -> {
            List<User> directFriends = friendRequestRepository.findAllFriends(user).stream()
                    .map(r -> r.getSender().getId().equals(user.getId()) ? r.getReceiver() : r.getSender())
                    .collect(Collectors.toList());

            int friendCount = directFriends.size();
            Set<Integer> uniqueFoF = new HashSet<>();

            for (User friend : directFriends) {
                List<User> friendsOfFriend = friendRequestRepository.findAllFriends(friend).stream()
                        .map(r -> r.getSender().getId().equals(friend.getId()) ? r.getReceiver() : r.getSender())
                        .collect(Collectors.toList());

                for (User fof : friendsOfFriend) {
                    if (!fof.getId().equals(user.getId())) {
                        uniqueFoF.add(fof.getId());
                    }
                }
            }

            // --- FIXED: Added .toString() for Gender to convert Enum to String ---
            return new UserFriendStatsDto(
                    user.getUsername(),
                    user.getFullName(),
                    user.getAddress(),
                    user.getDob() != null ? user.getDob().toLocalDate() : null,
                    user.getEmail(),
                    user.getGender() != null ? user.getGender().toString() : "",
                    friendCount,
                    uniqueFoF.size()
            );
        }).collect(Collectors.toList());
    }

    private UserDto convertToDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        return dto;
    }
}