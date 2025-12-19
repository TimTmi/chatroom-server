package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.MessageDto;
import com.example.chatroomserver.entity.*;
import com.example.chatroomserver.repository.ConversationMemberRepository;
import com.example.chatroomserver.repository.ConversationRepository;
import com.example.chatroomserver.repository.MessageRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepo;
    @Autowired private ConversationRepository conversationRepo;
    @Autowired private ConversationMemberRepository memberRepo;
    @Autowired private UserRepository userRepo;

    public MessageDto sendMessage(Integer conversationId, Integer senderId, String content) {

        if (!memberRepo.existsByConversationIdAndUserId(conversationId, senderId)) {
            throw new RuntimeException("User not in conversation");
        }

        Conversation convo = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message msg = new Message();
        msg.setConversation(convo);
        msg.setSender(sender);
        msg.setContent(content);

        Message saved = messageRepo.save(msg);
        return toDto(saved);
    }

    public List<MessageDto> getMessages(Integer conversationId, Integer userId) {

        if (!memberRepo.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Access denied");
        }

        return messageRepo
                .findByConversationIdAndIsDeletedFalseOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private MessageDto toDto(Message msg) {
        MessageDto dto = new MessageDto();
        dto.setId(msg.getId());
        dto.setConversationId(msg.getConversation().getId());
        dto.setSenderId(msg.getSender().getId());
        dto.setSenderName(
                msg.getSender().getFullName() != null
                        ? msg.getSender().getFullName()
                        : msg.getSender().getUsername()
        );
        dto.setContent(msg.getContent());
        dto.setSentAt(msg.getSentAt());
        return dto;
    }

    @Transactional
    public void deleteMessage(Integer messageId, Integer userId) {

        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Conversation convo = msg.getConversation();

        // Must be a member of the conversation
        if (!memberRepo.existsByConversationIdAndUserId(convo.getId(), userId)) {
            throw new RuntimeException("Access denied");
        }

        boolean canDelete = false;

        // Sender can always delete their own message
        if (msg.getSender().getId().equals(userId)) {
            canDelete = true;
        }

        // Admins can delete in group chats
        if (!canDelete && convo.getType() == ConversationType.GROUP) {
            canDelete = memberRepo.findByConversationId(convo.getId()).stream()
                    .anyMatch(m ->
                            m.getUser().getId().equals(userId) &&
                                    m.getRole() == ConversationMember.Role.ADMIN
                    );
        }

        if (!canDelete) {
            throw new RuntimeException("User not authorized to delete this message");
        }

        msg.setDeleted(true);
        messageRepo.save(msg);
    }

}
