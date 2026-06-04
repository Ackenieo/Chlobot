package com.chlobot.platform.app.task;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AgentTaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public AgentTaskRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public AgentTask create(AgentTask task) {
        jdbcTemplate.update("""
                        INSERT INTO agent_task (id, session_id, input, mode, status, risk_level, budget_json, result_json, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), ?, ?)
                        """,
                task.id(), task.sessionId(), task.input(), task.mode(), task.status().name(), task.riskLevel(),
                jsonMapper.toJson(task.budget()), jsonMapper.toJson(task.result()),
                Timestamp.from(task.createdAt()), Timestamp.from(task.updatedAt()));
        return task;
    }

    public Optional<AgentTask> findById(String id) {
        List<AgentTask> tasks = jdbcTemplate.query("""
                        SELECT id, session_id, input, mode, status, risk_level, budget_json, result_json,
                               error_code, error_message, created_at, updated_at
                        FROM agent_task WHERE id = ?
                        """,
                (rs, rowNum) -> new AgentTask(
                        rs.getString("id"),
                        rs.getString("session_id"),
                        rs.getString("input"),
                        rs.getString("mode"),
                        AgentTaskStatus.valueOf(rs.getString("status")),
                        rs.getString("risk_level"),
                        jsonMapper.toMap(rs.getString("budget_json")),
                        jsonMapper.toMap(rs.getString("result_json")),
                        rs.getString("error_code"),
                        rs.getString("error_message"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()),
                id);
        return tasks.stream().findFirst();
    }

    public void updateStatus(String id, AgentTaskStatus status) {
        jdbcTemplate.update("UPDATE agent_task SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", status.name(), id);
    }

    public void updateResult(String id, AgentTaskStatus status, Map<String, Object> result) {
        jdbcTemplate.update("UPDATE agent_task SET status = ?, result_json = CAST(? AS JSON), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                status.name(), jsonMapper.toJson(result), id);
    }
}
