package com.chlobot.platform.app.tool;

import com.chlobot.platform.app.task.AgentEventType;
import com.chlobot.platform.app.task.AgentTaskEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ToolService {

    private final ToolCallRepository repository;
    private final AgentTaskEventRepository eventRepository;
    private final AuditLogRepository auditLogRepository;

    public ToolService(ToolCallRepository repository, AgentTaskEventRepository eventRepository,
                       AuditLogRepository auditLogRepository) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<AgentToolCall> listByTask(String taskId) {
        return repository.listByTask(taskId);
    }

    public Map<String, Object> confirm(String taskId, String callId, boolean approved, String reason) {
        repository.confirm(callId, approved, reason);
        AgentEventType eventType = approved ? AgentEventType.TOOL_CALL_APPROVED : AgentEventType.TOOL_CALL_REJECTED;
        eventRepository.append(taskId, eventType, Map.of("toolCallId", callId, "reason", reason == null ? "" : reason));
        auditLogRepository.append("api-user", approved ? "TOOL_CALL_APPROVED" : "TOOL_CALL_REJECTED",
                "agent_tool_call", callId, Map.of("reason", reason == null ? "" : reason));
        return Map.of("toolCallId", callId, "approved", approved, "reason", reason == null ? "" : reason);
    }

    public AgentToolCall createMock(String taskId, String toolName, ToolPermissionLevel level, boolean requiresConfirmation) {
        enforceGovernance(toolName, level, requiresConfirmation);
        AgentToolCall call = new AgentToolCall(UUID.randomUUID().toString(), taskId, toolName, level, Map.of(), Map.of(),
                requiresConfirmation ? "PENDING_CONFIRMATION" : "COMPLETED", requiresConfirmation, null, null);
        repository.insert(call);
        eventRepository.append(taskId,
                requiresConfirmation ? AgentEventType.TOOL_CONFIRMATION_REQUIRED : AgentEventType.TOOL_CALL_COMPLETED,
                Map.of("toolCallId", call.id(), "toolName", toolName, "permissionLevel", level.name()));
        auditLogRepository.append("system", "TOOL_CALL_REGISTERED", "agent_tool_call", call.id(),
                Map.of("toolName", toolName, "requiresConfirmation", requiresConfirmation));
        return call;
    }

    private void enforceGovernance(String toolName, ToolPermissionLevel level, boolean requiresConfirmation) {
        if (level.ordinal() >= ToolPermissionLevel.L3.ordinal() && !requiresConfirmation) {
            throw new IllegalArgumentException("Dangerous tool requires confirmation: " + toolName);
        }
        auditLogRepository.append("system", "TOOL_GOVERNANCE_CHECKED", "tool", toolName,
                Map.of("permissionLevel", level.name(), "requiresConfirmation", requiresConfirmation,
                        "idempotencyPolicy", "required-for-retry", "timeoutPolicy", "metadata-timeout-seconds"));
    }
}
