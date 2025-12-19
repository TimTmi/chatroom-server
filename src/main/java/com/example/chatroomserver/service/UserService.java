package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.LoginHistoryDto;
import com.example.chatroomserver.dto.UserDto;
import com.example.chatroomserver.entity.LoginHistory;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.LoginHistoryRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    // --- NEW: Get All Users (For Admin Panel) ---
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    // --- Helper to convert Entity -> DTO ---
    private UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());

        // Handle Enums safely
        dto.setGender(user.getGender() != null ? user.getGender().name() : "OTHER");
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : "ACTIVE");

        // Determine Role (Simple logic: if username is 'admin', they are ADMIN)
        dto.setRole(user.getUsername().equalsIgnoreCase("admin") ? "ADMIN" : "USER");

        // Handle Date (Prevent Array serialization issue)
        dto.setDob(user.getDob() != null ? user.getDob().toLocalDate() : null);

        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    // --- User Management ---

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

        if (dto.getGender() != null) {
            try {
                user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setGender(User.Gender.OTHER);
            }
        } else {
            user.setGender(User.Gender.OTHER);
        }

        if (dto.getDob() != null) {
            user.setDob(dto.getDob().atStartOfDay());
        }

        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }

    public User validate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return null;
        if (user.getPassword().equals(password)) return user;
        return null;
    }

    public User updateUser(Integer id, UserDto dto) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setAddress(dto.getAddress());

            if (dto.getGender() != null) {
                try {
                    user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    user.setGender(User.Gender.OTHER);
                }
            }

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

    // --- Login History (Fixed to use DTOs) ---

    public void logLogin(User user, String ipAddress) {
        if (user != null) {
            LoginHistory history = new LoginHistory(user, ipAddress);
            loginHistoryRepository.save(history);
        }
    }

    @Transactional(readOnly = true)
    public List<LoginHistoryDto> getSystemLoginHistory() {
        return loginHistoryRepository.findAllByOrderByLoginTimeDesc().stream()
                .map(this::convertHistoryToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoginHistoryDto> getUserLoginHistory(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("User not found");
        return loginHistoryRepository.findByUserOrderByLoginTimeDesc(user).stream()
                .map(this::convertHistoryToDto)
                .collect(Collectors.toList());
    }

    private LoginHistoryDto convertHistoryToDto(LoginHistory h) {
        return new LoginHistoryDto(
                h.getLoginTime().toString(), // Converts to String "2023-..." safely
                h.getUser().getUsername(),
                h.getUser().getFullName(),
                h.getIpAddress()
        );
    }

    // --- Password & Search ---

    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(oldPassword)) {
            return false;
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        return true;
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<UserDto> searchUsers(String query, Integer currentUserId) {
        List<User> users = userRepository.findByUsernameContainingOrFullNameContaining(query, query);
        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(this::convertUserToDto) // Reuse helper
                .collect(Collectors.toList());
    }

    public boolean resetPasswordAndSendEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;

        // 1. Generate random password
        String newPassword = generateRandomPassword(10);

        // 2. Save new password (plain for now, ideally hash it)
        user.setPassword(newPassword);
        userRepository.save(user);

        // 3. Send email
        sendEmail(user.getEmail(), newPassword);

        return true;
    }

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    private void sendEmail(String to, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your New Chatroom Password");
        message.setText("Hello,\n\nYour password has been reset. Your new password is:\n\n" +
                newPassword + "\n\nPlease change it after logging in.");
        mailSender.send(message);
    }

    public int[] getMonthlyUserRegistrations(int year) {
        int[] counts = new int[12];

        // Fetch all users created in the specified year
        List<User> users = userRepository.findAllByCreatedAtBetween(
                LocalDate.of(year, 1, 1).atStartOfDay(),
                LocalDate.of(year, 12, 31).atTime(23, 59, 59)
        );

        for (User u : users) {
            if (u.getCreatedAt() != null) {
                int month = u.getCreatedAt().getMonthValue(); // 1..12
                counts[month - 1]++;
            }
        }

        return counts;
    }
}