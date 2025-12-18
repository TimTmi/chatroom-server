package com.example.chatroomserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING, ACCEPTED, REJECTED, BLOCKED
    }

    // --- CONSTRUCTORS ---
    public FriendRequest() {}

    // Constructor for normal requests (Default PENDING)
    public FriendRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for Blocking (Custom Status) - FIXES "Cannot resolve constructor"
    public FriendRequest(User sender, User receiver, Status status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}