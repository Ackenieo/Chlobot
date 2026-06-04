package com.chlobot.platform.app.graph;

import java.time.Instant;
import java.util.Map;

public record AgentTaskNode(String id, String taskId, String name, String nodeType, NodeStatus status,
                            String owner, int priority, Map<String, Object> payload, Map<String, Object> result,
                            String errorMessage, Instant createdAt, Instant updatedAt) {
}
