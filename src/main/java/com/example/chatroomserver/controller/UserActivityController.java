package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.UserActivityDto;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.ConversationMemberRepository;
import com.example.chatroomserver.repository.LoginHistoryRepository;
import com.example.chatroomserver.repository.MessageRepository;
import com.example.chatroomserver.repository.UserRepository;
import com.example.chatroomserver.service.UserActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserActivityController {

    private final UserActivityService userActivityService;

    public UserActivityController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/activity")
    public ResponseEntity<List<UserActivityDto>> getUserActivity(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<UserActivityDto> activities = userActivityService.getUserActivity(start, end);
        return ResponseEntity.ok(activities);
    }
}
