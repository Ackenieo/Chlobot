package com.chlobot.platform.app.tool;

import org.springframework.ai.tool.StaticToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class SpringAiToolAdapter {

    private final ToolService toolService;

    public SpringAiToolAdapter(ToolService toolService) {
        this.toolService = toolService;
    }

    public SpringAiToolDescriptor describePlanAck() {
        return new SpringAiToolDescriptor(
                "plan-ack",
                "Confirm a generated plan before execution",
                "{\"approved\":true,\"reason\":\"optional\"}",
                ToolPermissionLevel.L1,
                false,
                true,
                15,
                0,
                true,
                true,
                Map.of(
                        "category", "task-governance",
                        "provider", "spring-ai",
                        "route", "internal-tool"
                ));
    }

    public ToolCallback planAckCallback(String taskId) {
        return FunctionToolCallback.builder("plan-ack", (Map<String, Object> input) ->
                        toolService.confirm(taskId,
                                String.valueOf(input.getOrDefault("toolCallId", "")),
                                Boolean.TRUE.equals(input.get("approved")),
                                String.valueOf(input.getOrDefault("reason", ""))))
                .description("Confirm or reject a plan acknowledgement through Java governance")
                .inputType(Map.class)
                .toolMetadata(DefaultToolMetadata.builder().returnDirect(false).build())
                .build();
    }

    public StaticToolCallbackProvider provider(String taskId) {
        return new StaticToolCallbackProvider(List.of(planAckCallback(taskId)));
    }

    public ToolDefinition definition(SpringAiToolDescriptor descriptor) {
        return DefaultToolDefinition.builder()
                .name(descriptor.name())
                .description(descriptor.description())
                .inputSchema(descriptor.inputSchema())
                .build();
    }
}
