package com.example.chatroomserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    // 1. For "Add Friend" (Prevents 500 Errors)
    Optional<FriendRequest> findBySenderIdAndReceiverId(Integer senderId, Integer receiverId);

    // 2. For "Friend Requests" Tab
    List<FriendRequest> findByReceiverIdAndStatus(Integer receiverId, FriendRequest.Status status);

    // 3. For "Friend List" Tab 
    @Query("SELECT r FROM FriendRequest r WHERE (r.sender = :user OR r.receiver = :user) AND r.status = 'ACCEPTED'")
    List<FriendRequest> findAllFriends(@Param("user") User user);

    // 4. For "Blocked Users" Tab
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);

    // 5. General Relationship Check
    @Query("SELECT r FROM FriendRequest r WHERE (r.sender = :u1 AND r.receiver = :u2) OR (r.sender = :u2 AND r.receiver = :u1)")
    List<FriendRequest> findRelationship(@Param("u1") User u1, @Param("u2") User u2);
}