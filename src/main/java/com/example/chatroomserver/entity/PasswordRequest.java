package com.example.chatroomserver.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "password_requests")
public class PasswordRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String newPassword;
    private LocalDate requestDate;

    public PasswordRequest() {
        this.requestDate = LocalDate.now();
    }

    public PasswordRequest(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
        this.requestDate = LocalDate.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
}