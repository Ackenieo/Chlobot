package com.chlobot.platform.app.graph;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/graphs")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @Operation(summary = "Write an Agent task DAG")
    @PostMapping
    public AgentTaskGraph write(@RequestBody GraphWriteRequest request) {
        return graphService.write(request);
    }

    @Operation(summary = "Read an Agent task DAG")
    @GetMapping("/{taskId}")
    public AgentTaskGraph graph(@PathVariable String taskId) {
        return graphService.graph(taskId);
    }

    @Operation(summary = "Visualize an Agent task DAG as ASCII")
    @GetMapping("/{taskId}/ascii")
    public String ascii(@PathVariable String taskId) {
        return graphService.ascii(taskId);
    }

    @Operation(summary = "List ready DAG nodes")
    @GetMapping("/{taskId}/ready-nodes")
    public List<AgentTaskNode> readyNodes(@PathVariable String taskId) {
        return graphService.readyNodes(taskId);
    }

    @Operation(summary = "Atomically claim a DAG node")
    @PostMapping("/{taskId}/nodes/{nodeId}/claim")
    public AgentTaskNode claim(@PathVariable String taskId, @PathVariable String nodeId,
                               @RequestBody Map<String, Object> request) {
        return graphService.claim(taskId, nodeId, request);
    }

    @Operation(summary = "Complete a DAG node")
    @PostMapping("/{taskId}/nodes/{nodeId}/complete")
    public AgentTaskNode complete(@PathVariable String taskId, @PathVariable String nodeId,
                                  @RequestBody Map<String, Object> result) {
        return graphService.complete(taskId, nodeId, result);
    }

    @Operation(summary = "Fail a DAG node")
    @PostMapping("/{taskId}/nodes/{nodeId}/fail")
    public AgentTaskNode fail(@PathVariable String taskId, @PathVariable String nodeId,
                              @RequestBody Map<String, Object> request) {
        return graphService.fail(taskId, nodeId, request);
    }

    @Operation(summary = "Execute ready DAG nodes")
    @PostMapping("/{taskId}/execute")
    public List<AgentTaskNode> execute(@PathVariable String taskId,
                                       @RequestParam(defaultValue = "2") int concurrency) {
        return graphService.execute(taskId, concurrency);
    }
}
