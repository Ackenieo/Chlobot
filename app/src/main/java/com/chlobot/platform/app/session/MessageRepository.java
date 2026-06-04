package com.chlobot.platform.app.session;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public MessageRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public ConversationMessage create(ConversationMessage message) {
        jdbcTemplate.update("""
                        INSERT INTO conversation_message (session_id, message_type, role, content, metadata, created_at, updated_at, is_delete)
                        VALUES (?, ?, ?, ?, CAST(? AS JSON), ?, ?, 0)
                        """,
                message.sessionId(), message.role(), message.role(), message.content(),
                jsonMapper.toJson(message.metadata()), Timestamp.from(message.createdAt()), Timestamp.from(message.createdAt()));
        return message;
    }

    public List<ConversationMessage> listBySession(String sessionId) {
        return jdbcTemplate.query("""
                        SELECT id, session_id, role, content, metadata, created_at
                        FROM conversation_message
                        WHERE session_id = ? AND is_delete = 0
                        ORDER BY created_at ASC, id ASC
                        """,
                (rs, rowNum) -> new ConversationMessage(
                        rs.getLong("id"),
                        rs.getString("session_id"),
                        rs.getString("role"),
                        rs.getString("content"),
                        jsonMapper.toMap(rs.getString("metadata")),
                        rs.getTimestamp("created_at").toInstant()),
                sessionId);
    }

    public void deleteBySession(String sessionId) {
        jdbcTemplate.update("UPDATE conversation_message SET is_delete = 1, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?", sessionId);
    }
}
