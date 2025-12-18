package com.example.chatroomserver.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDto {

    private Integer id;
    private String type; // PRIVATE or GROUP
    private String name;
    private Boolean isEncrypted;
    private LocalDateTime createdAt;

    private List<MemberDto> members;
    private MessageDto lastMessage;

    // --- Getters & Setters ---
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

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    public MessageDto getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageDto lastMessage) { this.lastMessage = lastMessage; }

    public static class MemberDto {
        private Integer id;
        private String username;
        private String fullName;

        public MemberDto(Integer id, String username, String fullName) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
        }

        public Integer getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
    }
}
