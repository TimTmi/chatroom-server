package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.FriendRequest;
import com.example.chatroomserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);

    // Find people I have blocked
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);

    @Query("SELECT case when count(r)> 0 then true else false end FROM FriendRequest r WHERE " +
            "(r.sender = ?1 AND r.receiver = ?2) OR (r.sender = ?2 AND r.receiver = ?1)")
    boolean existsByUsers(User u1, User u2);

    @Query("SELECT r FROM FriendRequest r WHERE (r.sender = ?1 OR r.receiver = ?1) AND r.status = 'ACCEPTED'")
    List<FriendRequest> findAllFriends(User user);

    // FIX: Return List instead of Optional to prevent crashes on duplicates
    @Query("SELECT r FROM FriendRequest r WHERE (r.sender = ?1 AND r.receiver = ?2) OR (r.sender = ?2 AND r.receiver = ?1)")
    List<FriendRequest> findRelationship(User u1, User u2);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequest.Status status);
}