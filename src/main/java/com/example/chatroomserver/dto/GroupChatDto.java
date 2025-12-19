package com.example.chatroomserver.dto;

import java.util.List;

public class GroupChatDto {
    private Integer id;
    private String groupName;
    private String createdAt;
    private List<String> adminUsernames;
    private List<String> memberNames;

    public GroupChatDto(Integer id, String groupName, String createdAt, List<String> adminUsernames, List<String> memberNames) {
        this.id = id;
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.adminUsernames = adminUsernames;
        this.memberNames = memberNames;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<String> getAdminUsernames() { return adminUsernames; }
    public void setAdminUsernames(List<String> adminUsernames) { this.adminUsernames = adminUsernames; }

    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }
}