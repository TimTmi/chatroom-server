package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.PasswordRequest;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> login(@RequestBody UserDto userDto) {
        boolean success = userService.validate(userDto.getUsername(), userDto.getPassword());
        if (success) return ResponseEntity.ok("Login successful");
        else return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Integer id) {
        return userRepo.findById(id);
    }

    // --- Admin Features: Add, Update, Lock, Delete ---

    // This handles "Add User" from Admin Dashboard (POST /api/users)
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        try {
            User updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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

    // --- Password Requests ---

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
}