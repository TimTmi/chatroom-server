package com.example.chatroomserver.controller;

import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    // Register new user
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        // You should hash passwords in real use
        return userRepo.save(user);
    }

    // Simple login check
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        Optional<User> userOpt = Optional.ofNullable(userRepo.findByUsername(username));
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return "Login successful for user ID: " + userOpt.get().getId();
        }
        return "Invalid username or password";
    }

    // List all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Integer id) {
        return userRepo.findById(id);
    }
}
