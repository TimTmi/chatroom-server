package com.example.chatroomserver.dto;

public class FriendshipDto {
    private String username;
    private String status;
    private String since;

    public FriendshipDto(String username, String status, String since) {
        this.username = username;
        this.status = status;
        this.since = since;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSince() { return since; }
    public void setSince(String since) { this.since = since; }
}