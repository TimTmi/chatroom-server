package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.PasswordRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.PasswordRequestRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRequestRepository passwordRequestRepository;

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public User registerUser(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());

        // Safe Gender Set
        if (dto.getGender() != null) {
            try {
                user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setGender(User.Gender.OTHER);
            }
        } else {
            user.setGender(User.Gender.OTHER);
        }

        // Safe DOB Set
        if (dto.getDob() != null) {
            user.setDob(dto.getDob().atStartOfDay());
        } else {
            user.setDob(null);
        }

        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }

    public boolean validate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }

    // --- UPDATED THIS FUNCTION ---
    public User updateUser(Integer id, UserDto dto) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setAddress(dto.getAddress());

            // 1. Update Gender
            if (dto.getGender() != null) {
                try {
                    user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // keep old gender or set to OTHER if invalid
                }
            }

            // 2. Update DOB
            if (dto.getDob() != null) {
                user.setDob(dto.getDob().atStartOfDay());
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public void lockUser(Integer id) {
        userRepository.findById(id).map(user -> {
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

    // --- Password Request Logic ---
    public List<PasswordRequest> getAllPasswordRequests() {
        return passwordRequestRepository.findAll();
    }

    public void approvePasswordReset(Integer requestId) {
        PasswordRequest request = passwordRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User user = userRepository.findByUsername(request.getUsername());
        if (user != null) {
            user.setPassword("123456");
            userRepository.save(user);
        }
        passwordRequestRepository.delete(request);
    }
}