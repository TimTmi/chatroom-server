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
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void sendRequest(Integer senderId, Integer receiverId) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();
        if (!friendRequestRepository.existsByUsers(sender, receiver)) {
            friendRequestRepository.save(new FriendRequest(sender, receiver, FriendRequest.Status.PENDING));
        }
    }

    public List<UserDto> getPendingRequests(Integer userId) {
        User receiver = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING)
                .stream().map(req -> {
                    UserDto dto = convertToDto(req.getSender());
                    dto.setId(req.getId()); // HACK: Use DTO ID to store REQUEST ID for accepting
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<UserDto> getFriendList(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return friendRequestRepository.findAllFriends(user).stream()
                .map(f -> convertToDto(f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender()))
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
}