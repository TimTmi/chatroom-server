package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {
    List<LoginHistory> findAllByOrderByLoginTimeDesc();
    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);
}