package com.example.chatroomserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chatroomserver.entity.Conversation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Query("""
    SELECT c FROM Conversation c
    WHERE c.type = 'PRIVATE'
    AND (
        SELECT COUNT(cm)
        FROM ConversationMember cm
        WHERE cm.conversation = c
        AND cm.user.id IN (:userAId, :userBId)
    ) = 2
    """)
    Conversation findPrivateConversationBetweenUsers(
            @Param("userAId") Integer userAId,
            @Param("userBId") Integer userBId
    );
}
