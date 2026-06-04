package com.chlobot.platform.app.tool;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ToolCallRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public ToolCallRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public List<AgentToolCall> listByTask(String taskId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, tool_name, permission_level, input_json, output_json, status,
                               requires_confirmation, confirmed_by, confirmed_at
                        FROM agent_tool_call WHERE task_id = ? ORDER BY created_at ASC
                        """,
                (rs, rowNum) -> new AgentToolCall(
                        rs.getString("id"), rs.getString("task_id"), rs.getString("tool_name"),
                        ToolPermissionLevel.valueOf(rs.getString("permission_level")),
                        jsonMapper.toMap(rs.getString("input_json")), jsonMapper.toMap(rs.getString("output_json")),
                        rs.getString("status"), rs.getBoolean("requires_confirmation"), rs.getString("confirmed_by"),
                        rs.getTimestamp("confirmed_at") == null ? null : rs.getTimestamp("confirmed_at").toInstant()),
                taskId);
    }

    public void confirm(String id, boolean approved, String reason) {
        jdbcTemplate.update("""
                        UPDATE agent_tool_call
                        SET status = ?, confirmed_by = 'api-user', confirmed_at = CURRENT_TIMESTAMP, rejection_reason = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                approved ? "APPROVED" : "REJECTED", reason, id);
    }

    public void insert(AgentToolCall call) {
        jdbcTemplate.update("""
                        INSERT INTO agent_tool_call (id, task_id, tool_name, permission_level, input_json, output_json, status,
                                                     requires_confirmation, confirmed_by, confirmed_at, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                call.id(), call.taskId(), call.toolName(), call.permissionLevel().name(),
                jsonMapper.toJson(call.input()), jsonMapper.toJson(call.output()), call.status(),
                call.requiresConfirmation() ? 1 : 0, call.confirmedBy(), call.confirmedAt());
    }
}
