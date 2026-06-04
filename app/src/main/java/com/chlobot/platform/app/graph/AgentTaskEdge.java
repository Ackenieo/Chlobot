package com.chlobot.platform.app.graph;

import java.time.Instant;

public record AgentTaskEdge(Long id, String taskId, String fromNodeId, String toNodeId, String edgeType,
                            Instant createdAt) {
}
