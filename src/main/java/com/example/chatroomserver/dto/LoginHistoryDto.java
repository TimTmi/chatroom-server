package com.example.chatroomserver.dto;

public class LoginHistoryDto {
    private String loginTime;
    private String username;
    private String fullName;
    private String ipAddress;

    public LoginHistoryDto(String loginTime, String username, String fullName, String ipAddress) {
        this.loginTime = loginTime;
        this.username = username;
        this.fullName = fullName;
        this.ipAddress = ipAddress;
    }

    // Getters
    public String getLoginTime() { return loginTime; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getIpAddress() { return ipAddress; }
}