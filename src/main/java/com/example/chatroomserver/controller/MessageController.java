package com.example.chatroomserver.controller;

import com.example.chatroomserver.entity.Message;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.entity.ChatGroup;
import com.example.chatroomserver.repository.MessageRepository;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.repository.ChatGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ChatGroupRepository groupRepo;

    // Send message to a user
    @PostMapping("/user")
    public Message sendMessageToUser(@RequestParam Integer senderId,
                                     @RequestParam Integer receiverId,
                                     @RequestParam String content) {

        Optional<User> senderOpt = userRepo.findById(senderId);
        Optional<User> receiverOpt = userRepo.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            throw new RuntimeException("Sender or receiver not found");
        }

        Message msg = new Message();
        msg.setSender(senderOpt.get());
        msg.setReceiver(receiverOpt.get());
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setIsDeleted(false);

        return messageRepo.save(msg);
    }

    // Send message to a group
    @PostMapping("/group")
    public Message sendMessageToGroup(@RequestParam Integer senderId,
                                      @RequestParam Integer groupId,
                                      @RequestParam String content) {

        Optional<User> senderOpt = userRepo.findById(senderId);
        Optional<ChatGroup> groupOpt = groupRepo.findById(groupId);

        if (senderOpt.isEmpty() || groupOpt.isEmpty()) {
            throw new RuntimeException("Sender or group not found");
        }

        Message msg = new Message();
        msg.setSender(senderOpt.get());
        msg.setGroup(groupOpt.get());
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setIsDeleted(false);

        return messageRepo.save(msg);
    }

    // Get messages for a user (received messages)
    @GetMapping("/user/{userId}")
    public List<Message> getMessagesForUser(@PathVariable Integer userId) {
        return messageRepo.findByReceiverIdAndIsDeletedFalse(userId);
    }

    // Get messages in a group
    @GetMapping("/group/{groupId}")
    public List<Message> getMessagesForGroup(@PathVariable Integer groupId) {
        return messageRepo.findByGroupIdAndIsDeletedFalse(groupId);
    }
}
