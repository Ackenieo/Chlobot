package com.chlobot.platform.app.tool;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public AuditLogRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void append(String actorId, String action, String targetType, String targetId, Map<String, Object> payload) {
        jdbcTemplate.update("""
                        INSERT INTO agent_audit_log (actor_id, action, target_type, target_id, payload_json, created_at)
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                actorId, action, targetType, targetId, jsonMapper.toJson(payload));
    }
}
