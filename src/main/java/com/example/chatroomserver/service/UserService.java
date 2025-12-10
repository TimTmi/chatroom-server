package com.example.chatroomserver.service;

import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public User registerUser(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // in production, hash this!
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
        user.setAddress(dto.getAddress());
        user.setDob(dto.getDob() != null ? dto.getDob().atStartOfDay() : null);
        user.setStatus(User.Status.ACTIVE);

        return userRepository.save(user);
    }

    public boolean validate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }

    public User updateUser(Integer id, UserDto dto) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setAddress(dto.getAddress());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public void lockUser(Integer id) {
        userRepository.findById(id).map(user -> {
            // Toggle logic: If locked -> unlock, If active -> lock
            if (user.getStatus() == User.Status.LOCKED) {
                user.setStatus(User.Status.ACTIVE);
            } else {
                user.setStatus(User.Status.LOCKED);
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public void deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }
}
