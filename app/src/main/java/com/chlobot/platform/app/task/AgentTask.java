package com.chlobot.platform.app.task;

import java.time.Instant;
import java.util.Map;

public record AgentTask(String id, String sessionId, String input, String mode, AgentTaskStatus status,
                        String riskLevel, Map<String, Object> budget, Map<String, Object> result,
                        String errorCode, String errorMessage, Instant createdAt, Instant updatedAt) {
}
