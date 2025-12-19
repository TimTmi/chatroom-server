package com.example.chatroomserver.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDto {
    private Integer id;
    private String type; // "PRIVATE" or "GROUP"
    private String name;
    private Boolean isEncrypted;
    private LocalDateTime createdAt;
    private MessageDto lastMessage;
    private List<MemberDto> members;

    // Getters and Setters for ConversationDto
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public MessageDto getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageDto lastMessage) { this.lastMessage = lastMessage; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    // --- INNER CLASS: MemberDto ---
    public static class MemberDto {
        private Integer id;
        private String username;
        private String fullName;
        private String role; // <--- ADDED THIS FIELD

        public MemberDto() {}

        public MemberDto(Integer id, String username, String fullName) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
        }

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        // --- ADDED GETTER AND SETTER FOR ROLE ---
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}