package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.ChangePasswordRequest;
import com.example.chatroomserver.dto.ForgotPasswordRequest; // Ensure you have this class from your friend
import com.example.chatroomserver.dto.LoginHistoryDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;
    private final UserService userService;

    public UserController(UserService userService, UserRepository userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    // --- 1. GET ALL USERS (Kept your fix for Admin Dashboard) ---
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepo.findAll().stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setAddress(user.getAddress());
            dto.setGender(user.getGender() != null ? user.getGender().name() : "OTHER");
            dto.setDob(user.getDob() != null ? user.getDob().toLocalDate() : null);
            dto.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE");

            // Fix: Explicitly set the date so Admin Panel doesn't show "N/A"
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

            return dto;
        }).collect(Collectors.toList());
    }

    // --- 2. ADD/REGISTER USER (Common) ---
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }

    // --- 3. LOGIN & AUTH (Common) ---
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return available ? ResponseEntity.ok().build() : ResponseEntity.status(409).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto loginRequest, @RequestHeader(value = "X-Forwarded-For", defaultValue = "unknown") String ipAddress) {
        User user = userService.validate(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            if (user.getStatus() == User.Status.LOCKED) {
                return ResponseEntity.status(403).body("Account is locked");
            }
            userService.logLogin(user, ipAddress);

            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setAddress(user.getAddress());
            dto.setGender(user.getGender() != null ? user.getGender().name() : "OTHER");
            dto.setDob(user.getDob() != null ? user.getDob().toLocalDate() : null);
            dto.setRole(user.getUsername().equalsIgnoreCase("admin") ? "ADMIN" : "USER");
            dto.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // --- 4. USER OPERATIONS (Common) ---
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user != null) {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setAddress(user.getAddress());
            dto.setGender(user.getGender() != null ? user.getGender().name() : "OTHER");
            dto.setDob(user.getDob() != null ? user.getDob().toLocalDate() : null);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable Integer id) {
        userService.lockUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String q, @RequestParam Integer userId) {
        return userService.searchUsers(q, userId);
    }

    @GetMapping("/login-history")
    public List<LoginHistoryDto> getLoginHistory() {
        return userService.getSystemLoginHistory();
    }

    @GetMapping("/{username}/login-history")
    public List<LoginHistoryDto> getUserLoginHistory(@PathVariable String username) {
        return userService.getUserLoginHistory(username);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(@PathVariable Integer id, @RequestBody ChangePasswordRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        if (success) return ResponseEntity.ok("Password changed successfully");
        else return ResponseEntity.status(401).body("Incorrect old password");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            boolean sent = userService.resetPasswordAndSendEmail(request.getEmail());
            if (sent) {
                return ResponseEntity.ok("A new password has been sent to your email.");
            } else {
                return ResponseEntity.status(404).body("Email not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/search")
    public List<UserDto> searchUsers(@RequestParam String q, @RequestParam Integer userId) {
        return userService.searchUsers(q, userId);
    }

    @GetMapping("/registrations")
    public ResponseEntity<int[]> getUserRegistrations(@RequestParam int year) {
        int[] monthlyCounts = userService.getMonthlyUserRegistrations(year);
        return ResponseEntity.ok(monthlyCounts);
    }

}