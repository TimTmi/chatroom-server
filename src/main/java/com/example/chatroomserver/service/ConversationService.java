package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.ConversationDto;
import com.example.chatroomserver.dto.MessageDto;
import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.entity.ConversationMember;
import com.example.chatroomserver.entity.ConversationType;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.ConversationMemberRepository;
import com.example.chatroomserver.repository.ConversationRepository;
import com.example.chatroomserver.repository.MessageRepository;
import com.example.chatroomserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepo;
    @Autowired
    private ConversationMemberRepository memberRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private MessageRepository messageRepo;

    /**
     * Create a 1–1 conversation (DM)
     * Reuse existing conversation if it already exists
     */
    @Transactional
    public Conversation createDirectConversation(Integer userAId, Integer userBId) {
        if (userAId.equals(userBId)) {
            throw new RuntimeException("Cannot create DM with yourself");
        }

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
    public Conversation createGroupConversation(Integer creatorId, List<Integer> memberIds) {
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

    private void addMember(Conversation conversation, Integer userId, ConversationMember.Role role) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ConversationMember cm = new ConversationMember();
        cm.setConversation(conversation);
        cm.setUser(user);
        cm.setRole(role);

        memberRepo.save(cm);
    }

    // --- DTO conversion ---

    public ConversationDto toDto(Conversation conversation) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType().name());
        dto.setName(conversation.getName());
        dto.setIsEncrypted(conversation.getIsEncrypted());
        dto.setCreatedAt(conversation.getCreatedAt());

        // Members
        List<ConversationMember> members = memberRepo.findByConversationId(conversation.getId());
        List<ConversationDto.MemberDto> memberDtos = members.stream()
                .map(m -> new ConversationDto.MemberDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getFullName()))
                .collect(Collectors.toList());
        dto.setMembers(memberDtos);

        // Last message
        var messages = messageRepo.findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(conversation.getId());
        if (!messages.isEmpty()) {
            var last = messages.get(messages.size() - 1);
            MessageDto lastMessageDto = new MessageDto();
            lastMessageDto.setId(last.getId());
            lastMessageDto.setConversationId(last.getConversation().getId());
            lastMessageDto.setSenderId(last.getSender().getId());
            lastMessageDto.setSenderName(
                    last.getSender().getFullName() != null
                            ? last.getSender().getFullName()
                            : last.getSender().getUsername()
            );
            lastMessageDto.setContent(last.getContent());
            lastMessageDto.setSentAt(last.getSentAt());
            dto.setLastMessage(lastMessageDto);
        }

        return dto;
    }

    public List<ConversationDto> getUserConversationsDto(Integer userId) {
        return memberRepo.findByUserId(userId)
                .stream()
                .map(m -> toDto(m.getConversation()))
                .collect(Collectors.toList());
    }
}
