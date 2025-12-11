package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.PasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRequestRepository extends JpaRepository<PasswordRequest, Integer> {
}