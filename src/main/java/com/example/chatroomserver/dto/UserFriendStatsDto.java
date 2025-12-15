package com.example.chatroomserver.dto;

import java.time.LocalDate;

public class UserFriendStatsDto {
    private String username;
    private String fullName;
    private String address;
    private LocalDate dob;
    private String email;   // <--- Added
    private String gender;  // <--- Added
    private int friendCount;
    private int friendsOfFriendsCount;

    public UserFriendStatsDto(String username, String fullName, String address, LocalDate dob, String email, String gender, int friendCount, int friendsOfFriendsCount) {
        this.username = username;
        this.fullName = fullName;
        this.address = address;
        this.dob = dob;
        this.email = email;
        this.gender = gender;
        this.friendCount = friendCount;
        this.friendsOfFriendsCount = friendsOfFriendsCount;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public LocalDate getDob() { return dob; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public int getFriendCount() { return friendCount; }
    public int getFriendsOfFriendsCount() { return friendsOfFriendsCount; }
}