package com.example.chatroomserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chatroomserver.entity.ConversationMember;

import java.util.List;

public interface ConversationMemberRepository
        extends JpaRepository<ConversationMember, Integer> {

    boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);

    List<ConversationMember> findByUserId(Integer userId);

    List<ConversationMember> findByConversationId(Integer conversationId);
}


