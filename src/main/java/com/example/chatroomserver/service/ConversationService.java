package com.example.chatroomserver.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chatroomserver.dto.ConversationDto;
import com.example.chatroomserver.dto.GroupChatDto;
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

@Service
public class ConversationService {

    @Autowired private ConversationRepository conversationRepo;
    @Autowired private ConversationMemberRepository memberRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private MessageRepository messageRepo;

    // --- 1. ADMIN METHODS ---

    public List<GroupChatDto> getAllGroups() {
        return conversationRepo.findAll().stream()
                .filter(c -> c.getType() == ConversationType.GROUP)
                .map(this::convertToGroupDto)
                .collect(Collectors.toList());
    }

    private GroupChatDto convertToGroupDto(Conversation c) {
        List<ConversationMember> members = memberRepo.findByConversationId(c.getId());

        List<String> adminUsernames = members.stream()
                .filter(m -> m.getRole() == ConversationMember.Role.ADMIN)
                .map(m -> m.getUser().getUsername())
                .collect(Collectors.toList());

        List<String> memberNames = members.stream()
                .map(m -> m.getUser().getUsername())
                .collect(Collectors.toList());

        return new GroupChatDto(
                c.getId(),
                c.getName() != null ? c.getName() : "Unnamed Group",
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : LocalDateTime.now().toString(),
                adminUsernames,
                memberNames
        );
    }

    // --- 2. CREATION METHODS ---

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
    public Conversation createGroupConversation(Integer creatorId, String groupName, List<Integer> memberIds, List<Integer> adminIds) {
        if (memberIds == null || memberIds.isEmpty()) throw new RuntimeException("Group must have members");

        Conversation convo = new Conversation();
        convo.setType(ConversationType.GROUP);
        convo.setName(groupName);
        convo.setCreatedAt(LocalDateTime.now());
        conversationRepo.save(convo);
        addMember(convo, creatorId, ConversationMember.Role.ADMIN);

        for (Integer userId : memberIds) {
            if (!userId.equals(creatorId)) {
                ConversationMember.Role role = ConversationMember.Role.MEMBER;
                if (adminIds != null && adminIds.contains(userId)) {
                    role = ConversationMember.Role.ADMIN;
                }
                addMember(convo, userId, role);
            }
        }
        return convo;
    }

    private void addMember(Conversation conversation, Integer userId, ConversationMember.Role role) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ConversationMember cm = new ConversationMember();
        cm.setConversation(conversation);
        cm.setUser(user);
        cm.setRole(role);
        memberRepo.save(cm);
    }

    // --- 3. FRIEND'S CHAT DTO LOGIC ---

    public List<ConversationDto> getUserConversationsDto(Integer userId) {
        return memberRepo.findByUserId(userId)
                .stream()
                .map(m -> toDto(m.getConversation()))
                .collect(Collectors.toList());
    }

    public ConversationDto toDto(Conversation conversation) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setType(conversation.getType().name());
        dto.setName(conversation.getName());
        dto.setIsEncrypted(conversation.getIsEncrypted());
        dto.setCreatedAt(conversation.getCreatedAt());

        List<ConversationMember> members = memberRepo.findByConversationId(conversation.getId());
        List<ConversationDto.MemberDto> memberDtos = members.stream()
                .map(m -> {
                    ConversationDto.MemberDto memberDto = new ConversationDto.MemberDto(
                            m.getUser().getId(),
                            m.getUser().getUsername(),
                            m.getUser().getFullName());
                    memberDto.setRole(m.getRole().name());

                    return memberDto;
                })
                .collect(Collectors.toList());
        dto.setMembers(memberDtos);

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

    // --- 4. DELETE METHOD ---
    @Transactional
    public void deleteConversation(Integer conversationId, Integer userId) {
        Conversation convo = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        List<ConversationMember> members = memberRepo.findByConversationId(conversationId);
        boolean canDelete = false;

        if (convo.getType() == ConversationType.PRIVATE) {
            canDelete = members.stream().anyMatch(m -> m.getUser().getId().equals(userId));
        } else if (convo.getType() == ConversationType.GROUP) {
            canDelete = members.stream()
                    .anyMatch(m -> m.getUser().getId().equals(userId) && m.getRole() == ConversationMember.Role.ADMIN);
        }

        if (!canDelete) throw new RuntimeException("User not authorized to delete this conversation");

        messageRepo.deleteAllByConversationId(conversationId);
        memberRepo.deleteAll(members);
        conversationRepo.delete(convo);
    }

    // --- 5. UPDATE GROUP METHOD (New) ---
    @Transactional
    public void updateGroupConversation(Integer conversationId, String groupName, List<Integer> memberIds, List<Integer> adminIds) {
        Conversation convo = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 1. Update Name
        convo.setName(groupName);
        conversationRepo.save(convo);

        // 2. Sync Members
        List<ConversationMember> currentDbMembers = memberRepo.findByConversationId(conversationId);

        // A. Remove members who are NOT in the new list
        for (ConversationMember cm : currentDbMembers) {
            if (!memberIds.contains(cm.getUser().getId())) {
                memberRepo.delete(cm);
            }
        }

        // B. Add new members or Update existing roles
        for (Integer userId : memberIds) {
            ConversationMember cm = memberRepo.findByConversationIdAndUserId(conversationId, userId);

            if (cm == null) {
                User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                cm = new ConversationMember();
                cm.setConversation(convo);
                cm.setUser(user);
                cm.setJoinedAt(LocalDateTime.now());
            }

            // Update Role
            if (adminIds != null && adminIds.contains(userId)) {
                cm.setRole(ConversationMember.Role.ADMIN);
            } else {
                cm.setRole(ConversationMember.Role.MEMBER);
            }

            memberRepo.save(cm);
        }
    }
}