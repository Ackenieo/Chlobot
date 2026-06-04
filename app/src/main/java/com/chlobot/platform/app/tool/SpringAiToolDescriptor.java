package com.chlobot.platform.app.tool;

import java.util.Map;

public record SpringAiToolDescriptor(String name, String description, String inputSchema,
                                     ToolPermissionLevel permissionLevel, boolean readOnly,
                                     boolean requiresConfirmation, int timeoutSeconds,
                                     int maxRetries, boolean idempotent,
                                     boolean springAiCompatible, Map<String, Object> governance) {
}
