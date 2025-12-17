package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.GroupChatDto;
import com.example.chatroomserver.entity.GroupChat;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.GroupChatRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupChatService {

    @Autowired private GroupChatRepository groupChatRepository;
    @Autowired private UserRepository userRepository;

    public void createGroup(Integer adminId, String groupName, List<Integer> memberIds) {
        User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin not found"));
        GroupChat group = new GroupChat(groupName, admin);

        List<User> members = userRepository.findAllById(memberIds);
        for (User member : members) {
            group.addMember(member);
        }
        groupChatRepository.save(group);
    }

    // --- NEW ADMIN METHOD ---
    public List<GroupChatDto> getAllGroups() {
        return groupChatRepository.findAll().stream().map(group -> {
            List<String> memberNames = group.getMembers().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());

            return new GroupChatDto(
                    group.getId(),
                    group.getName(),
                    group.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    group.getAdmin().getUsername(),
                    memberNames
            );
        }).collect(Collectors.toList());
    }
}