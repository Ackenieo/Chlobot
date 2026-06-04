package com.chlobot.platform.app.task;

import java.time.Instant;
import java.util.Map;

public record AgentTaskEvent(Long id, String taskId, AgentEventType eventType, long sequenceNo,
                             Map<String, Object> payload, Instant createdAt) {
}
