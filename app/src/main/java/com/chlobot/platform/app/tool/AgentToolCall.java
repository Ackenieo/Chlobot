package com.chlobot.platform.app.tool;

import java.time.Instant;
import java.util.Map;

public record AgentToolCall(String id, String taskId, String toolName, ToolPermissionLevel permissionLevel,
                            Map<String, Object> input, Map<String, Object> output, String status,
                            boolean requiresConfirmation, String confirmedBy, Instant confirmedAt) {
}
