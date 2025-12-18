package com.example.chatroomserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chatroomserver.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {}
