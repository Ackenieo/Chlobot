package com.chlobot.platform.app.tool;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class ToolController {

    private final ToolService toolService;
    private final SpringAiToolAdapter springAiToolAdapter;

    public ToolController(ToolService toolService, SpringAiToolAdapter springAiToolAdapter) {
        this.toolService = toolService;
        this.springAiToolAdapter = springAiToolAdapter;
    }

    @Operation(summary = "List task tool calls")
    @GetMapping("/agent/tasks/{taskId}/tool-calls")
    public List<AgentToolCall> list(@PathVariable String taskId) {
        return toolService.listByTask(taskId);
    }

    @Operation(summary = "Confirm or reject a tool call")
    @PostMapping("/agent/tasks/{taskId}/tool-calls/{toolCallId}/confirm")
    public Map<String, Object> confirm(@PathVariable String taskId, @PathVariable String toolCallId,
                                       @RequestBody Map<String, Object> request) {
        boolean approved = Boolean.TRUE.equals(request.get("approved"));
        return toolService.confirm(taskId, toolCallId, approved, String.valueOf(request.getOrDefault("reason", "")));
    }

    @Operation(summary = "Expose Spring AI compatible tool descriptors")
    @GetMapping("/agent/tools/spring-ai/plan-ack")
    public SpringAiToolDescriptor describePlanAck() {
        return springAiToolAdapter.describePlanAck();
    }
}
