package com.chlobot.platform.app.task;

import com.chlobot.platform.agent.model.ModelClient;
import com.chlobot.platform.agent.model.ModelRequest;
import com.chlobot.platform.agent.model.ModelResponse;
import com.chlobot.platform.app.tool.AgentToolCall;
import com.chlobot.platform.app.tool.ToolPermissionLevel;
import com.chlobot.platform.app.tool.ToolService;
import com.chlobot.platform.common.error.BadRequestException;
import com.chlobot.platform.common.error.NotFoundException;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentTaskService {

    private final AgentTaskRepository taskRepository;
    private final AgentTaskEventRepository eventRepository;
    private final ToolService toolService;
    private final ModelClient modelClient;

    public AgentTaskService(AgentTaskRepository taskRepository, AgentTaskEventRepository eventRepository,
                            ToolService toolService, ModelClient modelClient) {
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.toolService = toolService;
        this.modelClient = modelClient;
    }

    public CreateAgentTaskResponse create(CreateAgentTaskRequest request) {
        Instant now = Instant.now();
        AgentTask task = new AgentTask(UUID.randomUUID().toString(), request.sessionId(), request.input(),
                request.mode() == null ? "CHAT" : request.mode(), AgentTaskStatus.PENDING, "LOW",
                request.budget() == null ? Map.of() : request.budget(), Map.of(), null, null, now, now);
        taskRepository.create(task);
        eventRepository.append(task.id(), AgentEventType.TASK_CREATED, Map.of("taskId", task.id(), "status", task.status().name()));
        eventRepository.append(task.id(), AgentEventType.STATUS_CHANGED, Map.of("to", task.status().name()));
        if (Boolean.TRUE.equals(request.requiresPlanConfirmation())) {
            ModelResponse plan = planWithSpringAi(task);
            transition(task.id(), AgentTaskStatus.PENDING, AgentTaskStatus.ANALYZING);
            transition(task.id(), AgentTaskStatus.ANALYZING, AgentTaskStatus.PLANNING);
            transition(task.id(), AgentTaskStatus.PLANNING, AgentTaskStatus.PENDING_CONFIRMATION);
            eventRepository.append(task.id(), AgentEventType.PLAN_GENERATED, Map.of(
                    "requiresConfirmation", true,
                    "content", plan.content(),
                    "provider", plan.provider(),
                    "model", plan.model(),
                    "mock", plan.mock(),
                    "route", "spring-ai-model-client"));
            return new CreateAgentTaskResponse(task.id(), AgentTaskStatus.PENDING_CONFIRMATION,
                    "/api/agent/tasks/" + task.id() + "/events",
                    Map.of("sessionId", task.sessionId(), "input", task.input(), "mode", task.mode()), now);
        }
        ModelResponse execution = executeWithSpringAi(task);
        transition(task.id(), AgentTaskStatus.PENDING, AgentTaskStatus.ANALYZING);
        transition(task.id(), AgentTaskStatus.ANALYZING, AgentTaskStatus.PLANNING);
        transition(task.id(), AgentTaskStatus.PLANNING, AgentTaskStatus.EXECUTING);
        eventRepository.append(task.id(), AgentEventType.PARTIAL_RESULT, Map.of(
                "content", execution.content(),
                "provider", execution.provider(),
                "model", execution.model(),
                "mock", execution.mock(),
                "route", "spring-ai-model-client"));
        Map<String, Object> result = Map.of(
                "content", execution.content(),
                "provider", execution.provider(),
                "model", execution.model(),
                "mock", execution.mock(),
                "route", "spring-ai-model-client");
        taskRepository.updateResult(task.id(), AgentTaskStatus.COMPLETED, result);
        eventRepository.append(task.id(), AgentEventType.COMPLETED, result);
        return new CreateAgentTaskResponse(task.id(), AgentTaskStatus.COMPLETED,
                "/api/agent/tasks/" + task.id() + "/events",
                Map.of("sessionId", task.sessionId(), "input", task.input(), "mode", task.mode()), now);
    }

    public AgentTask get(String taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
    }

    public List<AgentTaskEvent> events(String taskId) {
        get(taskId);
        return eventRepository.listByTask(taskId);
    }

    public Map<String, Object> confirmPlan(String taskId, Map<String, Object> request) {
        AgentTask task = get(taskId);
        if (!Boolean.TRUE.equals(request.get("approved"))) {
            taskRepository.updateStatus(taskId, AgentTaskStatus.CANCELLED);
            eventRepository.append(taskId, AgentEventType.CANCELLED, Map.of("taskId", taskId, "reason", "plan rejected"));
            return Map.of("taskId", taskId, "approved", false, "status", AgentTaskStatus.CANCELLED.name());
        }
        transition(taskId, task.status(), AgentTaskStatus.CONFIRMED);
        AgentToolCall toolCall = toolService.createMock(taskId, "plan-ack", ToolPermissionLevel.L1, false);
        transition(taskId, AgentTaskStatus.CONFIRMED, AgentTaskStatus.EXECUTING);
        ModelResponse execution = executeWithSpringAi(task);
        Map<String, Object> result = Map.of(
                "content", execution.content(),
                "provider", execution.provider(),
                "model", execution.model(),
                "mock", execution.mock(),
                "route", "spring-ai-model-client");
        taskRepository.updateResult(taskId, AgentTaskStatus.COMPLETED, result);
        eventRepository.append(taskId, AgentEventType.COMPLETED, result);
        return Map.of("taskId", taskId, "approved", true, "toolCallId", toolCall.id(), "status", AgentTaskStatus.COMPLETED.name());
    }

    public Map<String, Object> cancel(String taskId) {
        get(taskId);
        taskRepository.updateStatus(taskId, AgentTaskStatus.CANCELLED);
        eventRepository.append(taskId, AgentEventType.CANCELLED, Map.of("taskId", taskId));
        return Map.of("taskId", taskId, "status", AgentTaskStatus.CANCELLED.name());
    }

    public Map<String, Object> retry(String taskId) {
        AgentTask task = get(taskId);
        if (task.status() != AgentTaskStatus.FAILED && task.status() != AgentTaskStatus.CANCELLED) {
            throw new BadRequestException("Only failed or cancelled tasks can be retried");
        }
        taskRepository.updateStatus(taskId, AgentTaskStatus.PENDING);
        eventRepository.append(taskId, AgentEventType.RETRY_REQUESTED, Map.of("taskId", taskId));
        return Map.of("taskId", taskId, "status", AgentTaskStatus.PENDING.name());
    }

    public Map<String, Object> result(String taskId) {
        AgentTask task = get(taskId);
        return Map.of("taskId", taskId, "status", task.status().name(), "result", task.result());
    }

    private ModelResponse planWithSpringAi(AgentTask task) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage("You are Chlobot's Java/Spring AI planning adapter. Return a concise execution plan. Do not call provider SDKs directly."),
                new UserMessage(task.input())));
        return modelClient.chat(new ModelRequest(prompt, null, Map.of("taskId", task.id(), "stage", "planning")));
    }

    private ModelResponse executeWithSpringAi(AgentTask task) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage("You are Chlobot's Java/Spring AI execution adapter. Execute the user task concisely through the configured ModelClient route."),
                new UserMessage(task.input())));
        return modelClient.chat(new ModelRequest(prompt, null, Map.of("taskId", task.id(), "stage", "execution")));
    }

    private void transition(String taskId, AgentTaskStatus from, AgentTaskStatus to) {
        TaskStateMachine.validateTransition(from, to);
        taskRepository.updateStatus(taskId, to);
        eventRepository.append(taskId, AgentEventType.STATUS_CHANGED, Map.of("from", from.name(), "to", to.name()));
    }
}
