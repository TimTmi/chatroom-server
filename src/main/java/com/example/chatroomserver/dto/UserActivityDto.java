package com.example.chatroomserver.dto;

public class UserActivityDto {
    private String username;
    private String fullname;
    private String createdAt;
    private int opens;
    private int people;
    private int groups;

    public UserActivityDto(String username, String fullname, int opens, int people, int groups, String createdAt) {
        this.username = username;
        this.fullname = fullname;
        this.opens = opens;
        this.people = people;
        this.groups = groups;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public String getFullname() { return fullname; }
    public int getOpens() { return opens; }
    public int getPeople() { return people; }
    public int getGroups() { return groups; }
    public String getCreatedAt() { return createdAt; }
}
