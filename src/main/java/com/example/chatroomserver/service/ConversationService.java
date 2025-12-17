package com.example.chatroomserver.service;

import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.entity.ConversationMember;
import com.example.chatroomserver.entity.ConversationType;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.ConversationMemberRepository;
import com.example.chatroomserver.repository.ConversationRepository;
import com.example.chatroomserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepo;
    @Autowired private ConversationMemberRepository memberRepo;
    @Autowired private UserRepository userRepo;

    /**
     * Create a 1–1 conversation (DM)
     * Reuse existing conversation if it already exists
     */
    @Transactional
    public Conversation createDirectConversation(Integer userAId, Integer userBId) {

        if (userAId.equals(userBId)) {
            throw new RuntimeException("Cannot create DM with yourself");
        }

        // OPTIONAL but recommended:
        // Check if DM already exists between these two users
        // (You need a custom query for this if you want deduplication)

        Conversation convo = new Conversation();
        convo.setType(ConversationType.PRIVATE);
        conversationRepo.save(convo);

        addMember(convo, userAId, ConversationMember.Role.ADMIN);
        addMember(convo, userBId, ConversationMember.Role.ADMIN);

        return convo;
    }

    /**
     * Create group conversation
     */
    @Transactional
    public Conversation createGroupConversation(
            Integer creatorId,
            List<Integer> memberIds
    ) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new RuntimeException("Group must have members");
        }

        Conversation convo = new Conversation();
        convo.setType(ConversationType.GROUP);
        conversationRepo.save(convo);

        addMember(convo, creatorId, ConversationMember.Role.ADMIN);

        for (Integer userId : memberIds) {
            if (!userId.equals(creatorId)) {
                addMember(convo, userId, ConversationMember.Role.MEMBER);
            }
        }

        return convo;
    }

    public List<Conversation> getUserConversations(Integer userId) {
        return memberRepo.findByUserId(userId)
                .stream()
                .map(ConversationMember::getConversation)
                .toList();
    }

    private void addMember(
            Conversation conversation,
            Integer userId,
            ConversationMember.Role role
    ) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ConversationMember cm = new ConversationMember();
        cm.setConversation(conversation);
        cm.setUser(user);
        cm.setRole(role);

        memberRepo.save(cm);
    }
}
