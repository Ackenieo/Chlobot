package com.chlobot.platform.app.memory;

import java.time.Instant;
import java.util.Map;

public record AgentMemory(String id, String memoryType, String content, String source, Double confidence,
                          Map<String, Object> metadata, Instant createdAt, Instant updatedAt, boolean deleted) {
}
