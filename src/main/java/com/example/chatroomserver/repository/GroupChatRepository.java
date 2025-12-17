package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRepository extends JpaRepository<GroupChat, Integer> {
}