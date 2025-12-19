package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.LoginHistory;
import com.example.chatroomserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {
    List<LoginHistory> findAllByOrderByLoginTimeDesc();
    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);
    List<LoginHistory> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end);
}