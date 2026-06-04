package com.chlobot.platform.app.graph;

import com.chlobot.platform.app.task.AgentEventType;
import com.chlobot.platform.app.task.AgentTaskEventRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class GraphService {

    private final GraphRepository repository;
    private final AgentTaskEventRepository taskEventRepository;
    private final ThreadPoolTaskExecutor graphTaskExecutor;

    public GraphService(GraphRepository repository, AgentTaskEventRepository taskEventRepository,
                        ThreadPoolTaskExecutor graphTaskExecutor) {
        this.repository = repository;
        this.taskEventRepository = taskEventRepository;
        this.graphTaskExecutor = graphTaskExecutor;
    }

    public AgentTaskGraph write(GraphWriteRequest request) {
        GraphWriteRequest normalized = new GraphWriteRequest(
                request.taskId() == null || request.taskId().isBlank() ? UUID.randomUUID().toString() : request.taskId(),
                request.nodes(), request.edges() == null ? List.of() : request.edges());
        AgentTaskGraph graph = repository.write(normalized);
        taskEventRepository.append(graph.taskId(), AgentEventType.PROGRESS, Map.of("stage", "graph-written", "nodeCount", graph.nodes().size()));
        return graph;
    }

    public AgentTaskGraph graph(String taskId) {
        return repository.graph(taskId);
    }

    public List<AgentTaskNode> readyNodes(String taskId) {
        return repository.readyNodes(taskId);
    }

    public AgentTaskNode claim(String taskId, String nodeId, Map<String, Object> request) {
        return repository.claim(taskId, nodeId, String.valueOf(request.getOrDefault("owner", "anonymous")));
    }

    public AgentTaskNode complete(String taskId, String nodeId, Map<String, Object> result) {
        AgentTaskNode node = repository.complete(taskId, nodeId, result);
        taskEventRepository.append(taskId, AgentEventType.PROGRESS, Map.of("stage", "node-completed", "nodeId", nodeId));
        return node;
    }

    public AgentTaskNode fail(String taskId, String nodeId, Map<String, Object> request) {
        AgentTaskNode node = repository.fail(taskId, nodeId, String.valueOf(request.getOrDefault("message", "failed")));
        taskEventRepository.append(taskId, AgentEventType.ERROR, Map.of("stage", "node-failed", "nodeId", nodeId));
        return node;
    }

    public List<AgentTaskNode> execute(String taskId, int concurrency) {
        String owner = "graph-executor";
        List<AgentTaskNode> claimed = repository.claimableNodes(taskId, concurrency).stream()
                .map(node -> repository.claim(taskId, node.id(), owner))
                .toList();
        List<String> completedNodeIds = claimed.stream()
                .map(node -> CompletableFuture.supplyAsync(() -> node.id(), graphTaskExecutor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        repository.completeClaimed(taskId, completedNodeIds, owner);
        List<AgentTaskNode> completed = completedNodeIds.stream()
                .map(nodeId -> repository.graph(taskId).nodes().stream()
                        .filter(node -> node.id().equals(nodeId))
                        .findFirst()
                        .orElseThrow())
                .toList();
        taskEventRepository.append(taskId, AgentEventType.PROGRESS, Map.of("stage", "graph-executed", "completed", completed.size(), "concurrency", Math.max(1, concurrency)));
        return completed;
    }

    public String ascii(String taskId) {
        return repository.ascii(taskId);
    }
}
