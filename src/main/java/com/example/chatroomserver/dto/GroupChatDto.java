package com.example.chatroomserver.dto;

import java.util.List;

public class GroupChatDto {
    private Integer id;
    private String name;
    private String createdAt;
    private String adminUsername;
    private List<String> memberUsernames;

    public GroupChatDto(Integer id, String name, String createdAt, String adminUsername, List<String> memberUsernames) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.adminUsername = adminUsername;
        this.memberUsernames = memberUsernames;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCreatedAt() { return createdAt; }
    public String getAdminUsername() { return adminUsername; }
    public List<String> getMemberUsernames() { return memberUsernames; }
}