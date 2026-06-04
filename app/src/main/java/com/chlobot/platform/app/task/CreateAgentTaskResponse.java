package com.chlobot.platform.app.task;

import java.time.Instant;
import java.util.Map;

public record CreateAgentTaskResponse(String taskId, AgentTaskStatus status, String streamUrl, Map<String, Object> task,
                                      Instant createdAt) {
}
