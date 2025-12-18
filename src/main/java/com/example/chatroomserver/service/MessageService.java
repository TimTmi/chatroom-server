package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.MessageDto;
import com.example.chatroomserver.entity.Conversation;
import com.example.chatroomserver.entity.Message;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.ConversationMemberRepository;
import com.example.chatroomserver.repository.ConversationRepository;
import com.example.chatroomserver.repository.MessageRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

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

}
