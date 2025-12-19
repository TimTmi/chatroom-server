package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.MessageDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageSearchService {

    private final JdbcTemplate jdbcTemplate;

    public MessageSearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MessageDto> searchAllMessages(Integer userId, String query) {
        if (query == null || query.isBlank()) return List.of();

        String sql = """
            SELECT m.id, m.conversation_id, m.sender_id, u.full_name AS sender_name,
                   m.content, m.sent_at
            FROM messages m
            JOIN users u ON m.sender_id = u.id
            JOIN conversation_members cm ON m.conversation_id = cm.conversation_id
            WHERE cm.user_id = ?
              AND m.is_deleted = false
              AND LOWER(m.content) LIKE LOWER(CONCAT('%', ?, '%'))
            ORDER BY m.sent_at ASC
        """;

        return jdbcTemplate.query(
                sql,
                new Object[]{userId, query},
                (rs, rowNum) -> {
                    MessageDto dto = new MessageDto();
                    dto.setId(rs.getInt("id"));
                    dto.setConversationId(rs.getInt("conversation_id"));
                    dto.setSenderId(rs.getInt("sender_id"));
                    dto.setSenderName(rs.getString("sender_name"));
                    dto.setContent(rs.getString("content"));
                    dto.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    return dto;
                }
        );
    }
}
