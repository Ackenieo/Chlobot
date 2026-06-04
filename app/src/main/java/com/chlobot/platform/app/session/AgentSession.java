package com.chlobot.platform.app.session;

import java.time.Instant;
import java.util.Map;

public record AgentSession(String id, String title, Map<String, Object> metadata, Instant createdAt, Instant updatedAt) {
}
