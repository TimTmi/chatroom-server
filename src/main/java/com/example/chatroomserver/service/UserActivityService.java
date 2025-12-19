package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.UserActivityDto;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActivityService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserActivityService(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserActivityDto> getUserActivity(LocalDate start, LocalDate end) {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.created_at,
                   COALESCE(l.opens, 0) AS opens,
                   COALESCE(p.people, 0) AS people,
                   COALESCE(g.groups, 0) AS groups
            FROM users u
            LEFT JOIN (
                SELECT user_id, COUNT(*) AS opens
                FROM login_history
                WHERE login_time BETWEEN ? AND ?
                GROUP BY user_id
            ) l ON u.id = l.user_id
            LEFT JOIN (
                SELECT cm.user_id, COUNT(DISTINCT m.sender_id) AS people
                FROM conversation_members cm
                JOIN messages m ON m.conversation_id = cm.conversation_id
                WHERE m.sent_at BETWEEN ? AND ?
                  AND m.sender_id <> cm.user_id
                GROUP BY cm.user_id
            ) p ON u.id = p.user_id
            LEFT JOIN (
                SELECT cm.user_id, COUNT(DISTINCT cm.conversation_id) AS groups
                FROM conversation_members cm
                JOIN messages m ON m.conversation_id = cm.conversation_id
                WHERE m.sent_at BETWEEN ? AND ?
                GROUP BY cm.user_id
            ) g ON u.id = g.user_id
            ORDER BY u.username
            """;

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay().minusSeconds(1);

        return jdbcTemplate.query(sql,
                new Object[]{startTime, endTime, startTime, endTime, startTime, endTime},
                (rs, rowNum) -> new UserActivityDto(
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getInt("opens"),
                        rs.getInt("people"),
                        rs.getInt("groups"),
                        rs.getTimestamp("created_at").toLocalDateTime().toLocalDate().toString()
                ));
    }
}
