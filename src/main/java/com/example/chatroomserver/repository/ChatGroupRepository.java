package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Integer> {
    ChatGroup findByName(String name);
}
