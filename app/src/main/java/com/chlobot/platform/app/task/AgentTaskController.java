package com.chlobot.platform.app.task;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/tasks")
public class AgentTaskController {

    private final AgentTaskService taskService;

    public AgentTaskController(AgentTaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create an Agent task")
    @PostMapping
    public CreateAgentTaskResponse create(@Valid @RequestBody CreateAgentTaskRequest request) {
        return taskService.create(request);
    }

    @Operation(summary = "Get an Agent task")
    @GetMapping("/{taskId}")
    public AgentTask get(@PathVariable String taskId) {
        return taskService.get(taskId);
    }

    @Operation(summary = "Replay Agent task events as SSE")
    @GetMapping(value = "/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable String taskId) throws IOException {
        SseEmitter emitter = new SseEmitter(30_000L);
        for (AgentTaskEvent event : taskService.events(taskId)) {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(event.sequenceNo()))
                    .name(event.eventType().name().toLowerCase())
                    .data(event));
        }
        emitter.complete();
        return emitter;
    }

    @Operation(summary = "Replay Agent task timeline")
    @GetMapping("/{taskId}/timeline")
    public List<AgentTaskEvent> timeline(@PathVariable String taskId) {
        return taskService.events(taskId);
    }

    @Operation(summary = "Confirm a generated plan")
    @PostMapping("/{taskId}/confirm-plan")
    public Map<String, Object> confirmPlan(@PathVariable String taskId, @RequestBody Map<String, Object> request) {
        return taskService.confirmPlan(taskId, request);
    }

    @Operation(summary = "Cancel an Agent task")
    @PostMapping("/{taskId}/cancel")
    public Map<String, Object> cancel(@PathVariable String taskId) {
        return taskService.cancel(taskId);
    }

    @Operation(summary = "Retry an Agent task")
    @PostMapping("/{taskId}/retry")
    public Map<String, Object> retry(@PathVariable String taskId) {
        return taskService.retry(taskId);
    }

    @Operation(summary = "Get Agent task result")
    @GetMapping("/{taskId}/result")
    public Map<String, Object> result(@PathVariable String taskId) {
        AgentTask task = taskService.get(taskId);
        return Map.of("taskId", taskId, "status", task.status().name(), "result", task.result());
    }
}
