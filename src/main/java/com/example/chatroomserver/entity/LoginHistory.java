package com.example.chatroomserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String fullName;
    private String ipAddress; // <--- NEW FIELD
    private LocalDateTime loginTime;

    public LoginHistory() {}

    public LoginHistory(String username, String fullName, String ipAddress) {
        this.username = username;
        this.fullName = fullName;
        this.ipAddress = ipAddress;
        this.loginTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getIpAddress() { return ipAddress; } // <--- NEW GETTER
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; } // <--- NEW SETTER
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}