package com.example.chatroomserver.controller;

import com.example.chatroomserver.entity.ChatGroup;
import com.example.chatroomserver.entity.GroupMember;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.ChatGroupRepository;
import com.example.chatroomserver.repository.GroupMemberRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class ChatGroupController {

    @Autowired
    private ChatGroupRepository groupRepo;

    @Autowired
    private GroupMemberRepository memberRepo;

    @Autowired
    private UserRepository userRepo;

    // Create a group
    @PostMapping("/create")
    public ChatGroup createGroup(@RequestParam String name, @RequestParam Integer creatorId) {
        Optional<User> creatorOpt = userRepo.findById(creatorId);
        if (creatorOpt.isEmpty()) throw new RuntimeException("Creator not found");

        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setIsEncrypted(false);
        group.setCreatedAt(LocalDateTime.now());
        group = groupRepo.save(group);

        // Add creator as ADMIN
        GroupMember admin = new GroupMember();
        admin.setGroup(group);
        admin.setUser(creatorOpt.get());
        admin.setRole(GroupMember.Role.ADMIN);
        admin.setJoinedAt(LocalDateTime.now());
        memberRepo.save(admin);

        return group;
    }

    // Add member to group
    @PostMapping("/{groupId}/add")
    public GroupMember addMember(@PathVariable Integer groupId, @RequestParam Integer userId) {
        Optional<ChatGroup> groupOpt = groupRepo.findById(groupId);
        Optional<User> userOpt = userRepo.findById(userId);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) throw new RuntimeException("Group or user not found");

        GroupMember member = new GroupMember();
        member.setGroup(groupOpt.get());
        member.setUser(userOpt.get());
        member.setRole(GroupMember.Role.MEMBER);
        member.setJoinedAt(LocalDateTime.now());

        return memberRepo.save(member);
    }

    // List all groups
    @GetMapping
    public List<ChatGroup> getAllGroups() {
        return groupRepo.findAll();
    }
}
