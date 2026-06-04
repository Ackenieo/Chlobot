package com.chlobot.platform.app.tool;

import java.util.Map;

public record ToolMetadata(String name, String description, Map<String, Object> inputSchema,
                           Map<String, Object> outputSchema, ToolPermissionLevel permissionLevel,
                           boolean readOnly, boolean requiresConfirmation, int timeoutSeconds,
                           int maxRetries, boolean idempotent) {
}
