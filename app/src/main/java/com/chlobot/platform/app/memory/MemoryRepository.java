package com.chlobot.platform.app.memory;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class MemoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public MemoryRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public AgentMemory create(AgentMemory memory) {
        jdbcTemplate.update("""
                        INSERT INTO agent_memory (id, memory_type, content, source, confidence, metadata, created_at, updated_at, is_delete)
                        VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), ?, ?, ?)
                        """,
                memory.id(), memory.memoryType(), memory.content(), memory.source(), memory.confidence(),
                jsonMapper.toJson(memory.metadata()), Timestamp.from(memory.createdAt()), Timestamp.from(memory.updatedAt()),
                memory.deleted() ? 1 : 0);
        return memory;
    }

    public List<AgentMemory> search(String query, String type) {
        String sql = """
                SELECT id, memory_type, content, source, confidence, metadata, created_at, updated_at, is_delete
                FROM agent_memory
                WHERE is_delete = 0
                AND (? IS NULL OR memory_type = ?)
                AND (? IS NULL OR content LIKE CONCAT('%', ?, '%'))
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AgentMemory(
                        rs.getString("id"), rs.getString("memory_type"), rs.getString("content"),
                        rs.getString("source"), rs.getDouble("confidence"), jsonMapper.toMap(rs.getString("metadata")),
                        rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant(),
                        rs.getBoolean("is_delete")),
                type, type, query, query);
    }

    public Optional<AgentMemory> findById(String id) {
        List<AgentMemory> result = jdbcTemplate.query("""
                        SELECT id, memory_type, content, source, confidence, metadata, created_at, updated_at, is_delete
                        FROM agent_memory WHERE id = ?
                        """,
                (rs, rowNum) -> new AgentMemory(rs.getString("id"), rs.getString("memory_type"), rs.getString("content"),
                        rs.getString("source"), rs.getDouble("confidence"), jsonMapper.toMap(rs.getString("metadata")),
                        rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant(),
                        rs.getBoolean("is_delete")), id);
        return result.stream().findFirst();
    }

    public void update(AgentMemory memory) {
        jdbcTemplate.update("""
                        UPDATE agent_memory
                        SET memory_type = ?, content = ?, source = ?, confidence = ?, metadata = CAST(? AS JSON), updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                memory.memoryType(), memory.content(), memory.source(), memory.confidence(), jsonMapper.toJson(memory.metadata()), memory.id());
    }

    public void delete(String id) {
        jdbcTemplate.update("UPDATE agent_memory SET is_delete = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }
}
