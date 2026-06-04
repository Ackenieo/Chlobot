package com.chlobot.platform.app.rag;

import java.time.Instant;

public record KnowledgeBase(String id, String name, String description, boolean enabled,
                            Instant createdAt, Instant updatedAt) {
}
