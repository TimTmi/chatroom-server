package com.example.chatroomserver.dto;

import java.io.Serializable;

public class UserStatusDto implements Serializable {

    private Long userId;
    private boolean online;

    public UserStatusDto() {
    }

    public UserStatusDto(Long userId, boolean online) {
        this.userId = userId;
        this.online = online;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
