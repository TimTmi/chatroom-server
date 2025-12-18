package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.GroupChatDto;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired private ConversationRepository conversationRepo;
    @Autowired private ConversationMemberRepository memberRepo;
    @Autowired private UserRepository userRepo;

    // --- NEW: Admin Method to Get All Groups ---
    public List<GroupChatDto> getAllGroups() {
        return conversationRepo.findAll().stream()
                .filter(c -> c.getType() == ConversationType.GROUP)
                .map(this::convertToGroupDto)
                .collect(Collectors.toList());
    }

    private GroupChatDto convertToGroupDto(Conversation c) {
        List<ConversationMember> members = memberRepo.findByConversationId(c.getId());

        String adminUsername = members.stream()
                .filter(m -> m.getRole() == ConversationMember.Role.ADMIN)
                .map(m -> m.getUser().getUsername())
                .findFirst()
                .orElse("Unknown");

        List<String> memberNames = members.stream()
                .map(m -> m.getUser().getUsername())
                .collect(Collectors.toList());

        return new GroupChatDto(
                c.getId(),
                c.getName() != null ? c.getName() : "Unnamed Group",
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : "N/A",
                adminUsername,
                memberNames
        );
    }

    // --- User Methods ---

    @Transactional
    public Conversation createDirectConversation(Integer userAId, Integer userBId) {
        if (userAId.equals(userBId)) throw new RuntimeException("Cannot create DM with yourself");

        Conversation convo = new Conversation();
        convo.setType(ConversationType.PRIVATE);
        convo.setName("DM");
        convo.setCreatedAt(LocalDateTime.now());
        conversationRepo.save(convo);

        addMember(convo, userAId, ConversationMember.Role.ADMIN);
        addMember(convo, userBId, ConversationMember.Role.ADMIN);
        return convo;
    }

    @Transactional
    public Conversation createGroupConversation(Integer creatorId, String groupName, List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) throw new RuntimeException("Group must have members");

        Conversation convo = new Conversation();
        convo.setType(ConversationType.GROUP);
        convo.setName(groupName);
        convo.setCreatedAt(LocalDateTime.now());
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
        return memberRepo.findByUserId(userId).stream()
                .map(ConversationMember::getConversation)
                .toList();
    }

    private void addMember(Conversation conversation, Integer userId, ConversationMember.Role role) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ConversationMember cm = new ConversationMember();
        cm.setConversation(conversation);
        cm.setUser(user);
        cm.setRole(role);
        memberRepo.save(cm);
    }
}