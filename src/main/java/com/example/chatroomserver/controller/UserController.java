package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.ChangePasswordRequest;
import com.example.chatroomserver.dto.ForgotPasswordRequest;
import com.example.chatroomserver.dto.LoginHistoryDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;
    private final UserService userService;

    public UserController(UserService userService, UserRepository userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    // --- 1. GET ALL USERS (Fixes List Crash) ---
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    // --- 2. ADD USER (Fixes "Add User" Error) ---
    // This handles the POST /api/users request from your Admin Client
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User created successfully");
    }

    // --- 3. EXISTING REGISTER (Keep for Sign Up Page) ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }

    // --- 4. OTHER ENDPOINTS (Keep exactly as they were) ---

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
            dto.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE"); // Ensure Status is sent

            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

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
        if (success) {
            return ResponseEntity.ok("Password changed successfully");
        } else {
            return ResponseEntity.status(401).body("Incorrect old password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        System.out.println("!");
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
}