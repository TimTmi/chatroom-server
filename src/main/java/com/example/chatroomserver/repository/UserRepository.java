package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByUsernameContainingOrFullNameContaining(String username, String fullName);
}
