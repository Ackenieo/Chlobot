package com.chlobot.platform.app.observability;

import com.chlobot.platform.app.graph.GraphService;
import com.chlobot.platform.app.task.AgentTaskEvent;
import com.chlobot.platform.app.task.AgentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/observability")
public class ObservabilityController {

    private final AgentTaskService taskService;
    private final GraphService graphService;
    private final JdbcTemplate jdbcTemplate;

    public ObservabilityController(AgentTaskService taskService, GraphService graphService, JdbcTemplate jdbcTemplate) {
        this.taskService = taskService;
        this.graphService = graphService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "Get task timeline")
    @GetMapping("/tasks/{taskId}/timeline")
    public List<AgentTaskEvent> timeline(@PathVariable String taskId) {
        return taskService.events(taskId);
    }

    @Operation(summary = "Get platform metrics summary")
    @GetMapping("/metrics/summary")
    public Map<String, Object> metrics() {
        Integer taskCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM agent_task", Integer.class);
        Integer sessionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM agent_session WHERE is_delete = 0", Integer.class);
        return Map.of("taskCount", taskCount == null ? 0 : taskCount,
                "sessionCount", sessionCount == null ? 0 : sessionCount,
                "model", Map.of("calls", 0, "retries", 0, "fallbacks", 0, "estimatedTokens", 0),
                "rag", Map.of("hitAt5", 0, "recallAt5", 0, "mrr", 0, "citationCoverage", 0, "emptyRetrievalRate", 0),
                "tools", Map.of("pendingConfirmations", 0, "approvals", 0, "rejections", 0, "riskyCalls", 0),
                "memory", Map.of("confirmedFacts", 0, "selected", 0, "dropped", 0, "ruleConflicts", 0),
                "contextBudget", Map.of("windowTokens", 0, "outputReserve", 0, "usedTokens", 0, "droppedBlocks", 0),
                "status", "ok");
    }

    @Operation(summary = "Get Agent execution trace")
    @GetMapping("/tasks/{taskId}/trace")
    public Map<String, Object> trace(@PathVariable String taskId) {
        List<AgentTaskEvent> timeline = taskService.events(taskId);
        return Map.of(
                "taskId", taskId,
                "timeline", timeline,
                "dagNodes", graphService.graph(taskId).nodes(),
                "modelCalls", List.of(),
                "toolCalls", List.of(),
                "ragRetrievals", List.of(),
                "memoryAdvisorInjections", List.of(),
                "retries", List.of(),
                "errors", List.of()
        );
    }
}
