package com.chlobot.platform.app.rag;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class KnowledgeBaseRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeBaseRepository(@Qualifier("pgVectorJdbcTemplate") JdbcTemplate pgVectorJdbcTemplate) {
        this.jdbcTemplate = pgVectorJdbcTemplate;
    }

    public List<KnowledgeBase> findAll() {
        return jdbcTemplate.query("""
                        SELECT id, name, description, enabled, created_at, updated_at
                        FROM knowledge_base
                        WHERE is_delete = false
                        ORDER BY updated_at DESC
                        """,
                (rs, rowNum) -> new KnowledgeBase(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("enabled"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()));
    }

    public KnowledgeBase create(CreateKnowledgeBaseRequest request) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        jdbcTemplate.update("""
                        INSERT INTO knowledge_base (id, name, description, enabled, is_delete, created_at, updated_at)
                        VALUES (?, ?, ?, ?, false, ?, ?)
                        """,
                id, requireName(request.name()), blankToNull(request.description()), !Boolean.FALSE.equals(request.enabled()),
                Timestamp.from(now), Timestamp.from(now));
        return get(id);
    }

    public KnowledgeBase get(String id) {
        return jdbcTemplate.query("""
                        SELECT id, name, description, enabled, created_at, updated_at
                        FROM knowledge_base
                        WHERE id = ? AND is_delete = false
                        """,
                (rs, rowNum) -> new KnowledgeBase(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("enabled"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()),
                id).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Knowledge base does not exist: " + id));
    }

    public KnowledgeBase update(String id, UpdateKnowledgeBaseRequest request) {
        KnowledgeBase current = get(id);
        String nextName = request.name() == null || request.name().isBlank() ? current.name() : request.name().trim();
        String nextDescription = request.description() == null ? current.description() : blankToNull(request.description());
        boolean nextEnabled = request.enabled() == null ? current.enabled() : request.enabled();
        jdbcTemplate.update("""
                        UPDATE knowledge_base
                        SET name = ?, description = ?, enabled = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ? AND is_delete = false
                        """,
                nextName, nextDescription, nextEnabled, id);
        return get(id);
    }

    public void delete(String id) {
        get(id);
        jdbcTemplate.update("UPDATE knowledge_base SET is_delete = true, enabled = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?", id);
        jdbcTemplate.update("UPDATE document_embeddings SET is_delete = true, updated_at = CURRENT_TIMESTAMP WHERE knowledge_base_id = ?", id);
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Knowledge base name is required");
        }
        return name.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
