package com.example.chatroomserver.dto;

public class FriendshipDto {
    private Integer id;
    private String username;
    private String fullName;
    private String status;
    private String since;

    public FriendshipDto() {
    }

    public FriendshipDto(Integer id, String username, String fullName, String status, String since) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
        this.since = since;
    }

    // 3. Getters and Setters (Fixes "Cannot resolve method...")
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSince() { return since; }
    public void setSince(String since) { this.since = since; }
}