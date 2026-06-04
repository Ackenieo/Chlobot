package com.chlobot.platform.app.rag;

import java.time.Instant;
import java.util.Map;

public record DocumentEmbedding(Long id, String documentId, String chunkId, String title, String content,
                                Object embedding, Map<String, Object> metadata, Instant createdAt, Instant updatedAt,
                                String knowledgeBaseId, boolean enabled) {
}
