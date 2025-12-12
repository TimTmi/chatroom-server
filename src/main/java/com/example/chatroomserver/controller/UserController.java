package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.PasswordRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.chatroomserver.entity.LoginHistory;
import com.example.chatroomserver.dto.ChangePasswordRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;
    private final UserService userService;

    public UserController(UserService userService, UserRepository userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    // --- Basic User Management ---

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return available ? ResponseEntity.ok().build() : ResponseEntity.status(409).build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto, HttpServletRequest request) {
        boolean success = userService.validate(userDto.getUsername(), userDto.getPassword());
        if (success) {
            // 1. Log the IP
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            userService.logLogin(userDto.getUsername(), ip);

            // 2. FETCH AND RETURN THE ACTUAL USER (Crucial for Client)
            User user = userRepo.findByUsername(userDto.getUsername());
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @GetMapping("/login-history")
    public List<LoginHistory> getLoginHistory() {
        return userService.getSystemLoginHistory();
    }

    @GetMapping("/{username}/login-history")
    public List<LoginHistory> getUserLoginHistory(@PathVariable String username) {
        return userService.getUserLoginHistory(username);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Integer id) {
        return userRepo.findById(id);
    }

    // --- Admin & Update Features ---

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User created successfully");
    }

    // THIS IS THE CORRECT, ROBUST UPDATE METHOD (Duplicates Removed)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserDto updatedData) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Update fields safely (checking for nulls)
        if (updatedData.getFullName() != null) existingUser.setFullName(updatedData.getFullName());
        if (updatedData.getEmail() != null) existingUser.setEmail(updatedData.getEmail());
        if (updatedData.getAddress() != null) existingUser.setAddress(updatedData.getAddress());

        // Gender Update
        if (updatedData.getGender() != null) {
            try {
                existingUser.setGender(User.Gender.valueOf(updatedData.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid gender
            }
        }

        // Date Update
        if (updatedData.getDob() != null) {
            existingUser.setDob(updatedData.getDob().atStartOfDay());
        }

        userService.saveUser(existingUser);
        return ResponseEntity.ok("User updated successfully");
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<String> lockUser(@PathVariable Integer id) {
        try {
            userService.lockUser(id);
            return ResponseEntity.ok("User status updated");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Password Features ---

    @GetMapping("/password-requests")
    public List<PasswordRequest> getPasswordRequests() {
        return userService.getAllPasswordRequests();
    }

    @PostMapping("/password-requests/{id}/approve")
    public ResponseEntity<String> approveReset(@PathVariable Integer id) {
        try {
            userService.approvePasswordReset(id);
            return ResponseEntity.ok("Password reset approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(@PathVariable Integer id, @RequestBody ChangePasswordRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok("Password changed successfully");
        } else {
            return ResponseEntity.status(401).body("Incorrect old password");
        }
    }
}