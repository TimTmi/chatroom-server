package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByReceiverIdAndIsDeletedFalse(Integer receiverId);
    List<Message> findByGroup_IdAndIsDeletedFalse(Integer groupId);
}