package com.chlobot.platform.app.task;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public class AgentTaskEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public AgentTaskEventRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public AgentTaskEvent append(String taskId, AgentEventType eventType, Map<String, Object> payload) {
        Long next = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(sequence_no), 0) + 1 FROM agent_task_event WHERE task_id = ?",
                Long.class, taskId);
        long sequence = next == null ? 1L : next;
        Instant now = Instant.now();
        jdbcTemplate.update("""
                        INSERT INTO agent_task_event (task_id, event_type, sequence_no, payload_json, created_at)
                        VALUES (?, ?, ?, CAST(? AS JSON), ?)
                        """,
                taskId, eventType.name(), sequence, jsonMapper.toJson(payload), Timestamp.from(now));
        return new AgentTaskEvent(null, taskId, eventType, sequence, payload, now);
    }

    public List<AgentTaskEvent> listByTask(String taskId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, event_type, sequence_no, payload_json, created_at
                        FROM agent_task_event
                        WHERE task_id = ?
                        ORDER BY sequence_no ASC
                        """,
                (rs, rowNum) -> new AgentTaskEvent(
                        rs.getLong("id"),
                        rs.getString("task_id"),
                        AgentEventType.valueOf(rs.getString("event_type")),
                        rs.getLong("sequence_no"),
                        jsonMapper.toMap(rs.getString("payload_json")),
                        rs.getTimestamp("created_at").toInstant()),
                taskId);
    }
}
