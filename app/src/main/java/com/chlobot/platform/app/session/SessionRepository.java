package com.chlobot.platform.app.session;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public SessionRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public AgentSession create(AgentSession session) {
        jdbcTemplate.update("""
                        INSERT INTO agent_session (id, title, metadata, created_at, updated_at, is_delete)
                        VALUES (?, ?, CAST(? AS JSON), ?, ?, 0)
                        """,
                session.id(), session.title(), jsonMapper.toJson(session.metadata()),
                Timestamp.from(session.createdAt()), Timestamp.from(session.updatedAt()));
        return session;
    }

    public List<AgentSession> list(int limit, int offset) {
        return jdbcTemplate.query("""
                        SELECT id, title, metadata, created_at, updated_at
                        FROM agent_session
                        WHERE is_delete = 0
                        ORDER BY created_at DESC
                        LIMIT ? OFFSET ?
                        """,
                mapper(), limit, offset);
    }

    public Optional<AgentSession> findById(String id) {
        List<AgentSession> sessions = jdbcTemplate.query("""
                        SELECT id, title, metadata, created_at, updated_at
                        FROM agent_session
                        WHERE id = ? AND is_delete = 0
                        """,
                mapper(), id);
        return sessions.stream().findFirst();
    }

    public void delete(String id) {
        jdbcTemplate.update("UPDATE agent_session SET is_delete = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    private RowMapper<AgentSession> mapper() {
        return (rs, rowNum) -> new AgentSession(
                rs.getString("id"),
                rs.getString("title"),
                jsonMapper.toMap(rs.getString("metadata")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
