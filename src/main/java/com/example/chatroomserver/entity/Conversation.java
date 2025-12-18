package com.example.chatroomserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @Column(length = 191)
    private String name;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Initialize Default

    // --- getters & setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}