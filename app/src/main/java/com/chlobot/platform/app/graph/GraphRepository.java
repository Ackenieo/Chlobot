package com.chlobot.platform.app.graph;

import com.chlobot.platform.app.config.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
public class GraphRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public GraphRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public AgentTaskGraph write(GraphWriteRequest request) {
        jdbcTemplate.update("DELETE FROM agent_task_node_event WHERE task_id = ?", request.taskId());
        jdbcTemplate.update("DELETE FROM agent_task_edge WHERE task_id = ?", request.taskId());
        jdbcTemplate.update("DELETE FROM agent_task_node WHERE task_id = ?", request.taskId());
        for (GraphWriteRequest.NodeSpec node : request.nodes()) {
            jdbcTemplate.update("""
                            INSERT INTO agent_task_node (id, task_id, name, node_type, status, priority, payload_json)
                            VALUES (?, ?, ?, ?, ?, ?, CAST(? AS JSON))
                            """,
                    node.id(), request.taskId(), node.name(), node.nodeType(), NodeStatus.PENDING.name(),
                    node.priority(), jsonMapper.toJson(node.payload()));
            appendEvent(request.taskId(), node.id(), "NODE_CREATED", Map.of("name", node.name()));
        }
        for (GraphWriteRequest.EdgeSpec edge : request.edges()) {
            jdbcTemplate.update("""
                            INSERT INTO agent_task_edge (task_id, from_node_id, to_node_id, edge_type)
                            VALUES (?, ?, ?, ?)
                            """,
                    request.taskId(), edge.fromNodeId(), edge.toNodeId(), edge.edgeType() == null ? "BLOCKS" : edge.edgeType());
        }
        markReadyNodes(request.taskId());
        return graph(request.taskId());
    }

    public AgentTaskGraph graph(String taskId) {
        List<AgentTaskNode> nodes = nodes(taskId);
        List<AgentTaskEdge> edges = edges(taskId);
        GraphStatus status = status(nodes);
        return new AgentTaskGraph(taskId, status, nodes, edges, parallelGroups(nodes, edges));
    }

    public List<AgentTaskNode> readyNodes(String taskId) {
        markReadyNodes(taskId);
        return nodesByStatus(taskId, NodeStatus.READY);
    }

    public List<AgentTaskNode> claimableNodes(String taskId, int concurrency) {
        return readyNodes(taskId).stream().limit(Math.max(1, concurrency)).toList();
    }

    @Transactional
    public AgentTaskNode claim(String taskId, String nodeId, String owner) {
        int updated = jdbcTemplate.update("""
                        UPDATE agent_task_node SET status = ?, owner = ?
                        WHERE task_id = ? AND id = ? AND status = ?
                        """,
                NodeStatus.CLAIMED.name(), owner, taskId, nodeId, NodeStatus.READY.name());
        if (updated != 1) {
            throw new IllegalStateException("Node is not claimable: " + nodeId);
        }
        appendEvent(taskId, nodeId, "NODE_CLAIMED", Map.of("owner", owner));
        return node(taskId, nodeId);
    }

    @Transactional
    public AgentTaskNode complete(String taskId, String nodeId, Map<String, Object> result) {
        jdbcTemplate.update("""
                        UPDATE agent_task_node SET status = ?, result_json = CAST(? AS JSON), error_message = NULL
                        WHERE task_id = ? AND id = ?
                        """,
                NodeStatus.COMPLETED.name(), jsonMapper.toJson(result), taskId, nodeId);
        appendEvent(taskId, nodeId, "NODE_COMPLETED", result == null ? Map.of() : result);
        markReadyNodes(taskId);
        return node(taskId, nodeId);
    }

    @Transactional
    public AgentTaskNode fail(String taskId, String nodeId, String message) {
        jdbcTemplate.update("""
                        UPDATE agent_task_node SET status = ?, error_message = ?
                        WHERE task_id = ? AND id = ?
                        """,
                NodeStatus.FAILED.name(), message, taskId, nodeId);
        appendEvent(taskId, nodeId, "NODE_FAILED", Map.of("message", message == null ? "" : message));
        return node(taskId, nodeId);
    }

    public void completeClaimed(String taskId, Collection<String> nodeIds, String owner) {
        for (String nodeId : nodeIds) {
            jdbcTemplate.update("""
                            UPDATE agent_task_node SET status = ?, result_json = CAST(? AS JSON), error_message = NULL
                            WHERE task_id = ? AND id = ? AND status = ? AND owner = ?
                            """,
                    NodeStatus.COMPLETED.name(), jsonMapper.toJson(Map.of("executor", owner)), taskId, nodeId,
                    NodeStatus.CLAIMED.name(), owner);
            appendEvent(taskId, nodeId, "NODE_COMPLETED", Map.of("executor", owner));
        }
        markReadyNodes(taskId);
    }

    public List<AgentTaskNode> executeReady(String taskId, int concurrency) {
        List<AgentTaskNode> completed = new ArrayList<>();
        int max = Math.max(1, concurrency);
        for (AgentTaskNode node : readyNodes(taskId).stream().limit(max).toList()) {
            claim(taskId, node.id(), "graph-executor");
            completed.add(complete(taskId, node.id(), Map.of("executor", "graph-executor")));
        }
        return completed;
    }

    public String ascii(String taskId) {
        AgentTaskGraph graph = graph(taskId);
        StringBuilder builder = new StringBuilder();
        for (AgentTaskNode node : graph.nodes()) {
            builder.append(node.id()).append("[").append(node.status()).append("] ").append(node.name()).append('\n');
            graph.edges().stream().filter(edge -> edge.fromNodeId().equals(node.id()))
                    .forEach(edge -> builder.append("  -> ").append(edge.toNodeId()).append('\n'));
        }
        return builder.toString();
    }

    private void markReadyNodes(String taskId) {
        for (AgentTaskNode node : nodesByStatus(taskId, NodeStatus.PENDING)) {
            Integer blockers = jdbcTemplate.queryForObject("""
                            SELECT COUNT(*) FROM agent_task_edge e
                            JOIN agent_task_node n ON n.task_id = e.task_id AND n.id = e.from_node_id
                            WHERE e.task_id = ? AND e.to_node_id = ? AND n.status <> ?
                            """,
                    Integer.class, taskId, node.id(), NodeStatus.COMPLETED.name());
            if (blockers == null || blockers == 0) {
                jdbcTemplate.update("UPDATE agent_task_node SET status = ? WHERE task_id = ? AND id = ?",
                        NodeStatus.READY.name(), taskId, node.id());
                appendEvent(taskId, node.id(), "NODE_READY", Map.of());
            }
        }
    }

    private List<AgentTaskNode> nodes(String taskId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, name, node_type, status, owner, priority, payload_json, result_json, error_message, created_at, updated_at
                        FROM agent_task_node WHERE task_id = ? ORDER BY priority DESC, created_at ASC
                        """,
                (rs, rowNum) -> new AgentTaskNode(rs.getString("id"), rs.getString("task_id"), rs.getString("name"),
                        rs.getString("node_type"), NodeStatus.valueOf(rs.getString("status")), rs.getString("owner"),
                        rs.getInt("priority"), jsonMapper.toMap(rs.getString("payload_json")), jsonMapper.toMap(rs.getString("result_json")),
                        rs.getString("error_message"), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant()),
                taskId);
    }

    private List<AgentTaskNode> nodesByStatus(String taskId, NodeStatus status) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, name, node_type, status, owner, priority, payload_json, result_json, error_message, created_at, updated_at
                        FROM agent_task_node WHERE task_id = ? AND status = ? ORDER BY priority DESC, created_at ASC
                        """,
                (rs, rowNum) -> new AgentTaskNode(rs.getString("id"), rs.getString("task_id"), rs.getString("name"),
                        rs.getString("node_type"), NodeStatus.valueOf(rs.getString("status")), rs.getString("owner"),
                        rs.getInt("priority"), jsonMapper.toMap(rs.getString("payload_json")), jsonMapper.toMap(rs.getString("result_json")),
                        rs.getString("error_message"), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant()),
                taskId, status.name());
    }

    private AgentTaskNode node(String taskId, String nodeId) {
        return nodes(taskId).stream().filter(node -> node.id().equals(nodeId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));
    }

    private List<AgentTaskEdge> edges(String taskId) {
        return jdbcTemplate.query("""
                        SELECT id, task_id, from_node_id, to_node_id, edge_type, created_at
                        FROM agent_task_edge WHERE task_id = ? ORDER BY id ASC
                        """,
                (rs, rowNum) -> new AgentTaskEdge(rs.getLong("id"), rs.getString("task_id"), rs.getString("from_node_id"),
                        rs.getString("to_node_id"), rs.getString("edge_type"), rs.getTimestamp("created_at").toInstant()),
                taskId);
    }

    private void appendEvent(String taskId, String nodeId, String eventType, Map<String, Object> payload) {
        jdbcTemplate.update("""
                        INSERT INTO agent_task_node_event (task_id, node_id, event_type, payload_json, created_at)
                        VALUES (?, ?, ?, CAST(? AS JSON), ?)
                        """,
                taskId, nodeId, eventType, jsonMapper.toJson(payload), Timestamp.from(java.time.Instant.now()));
    }

    private GraphStatus status(List<AgentTaskNode> nodes) {
        if (nodes.stream().anyMatch(node -> node.status() == NodeStatus.FAILED)) {
            return GraphStatus.FAILED;
        }
        if (!nodes.isEmpty() && nodes.stream().allMatch(node -> node.status() == NodeStatus.COMPLETED)) {
            return GraphStatus.COMPLETED;
        }
        if (nodes.stream().anyMatch(node -> node.status() == NodeStatus.CLAIMED || node.status() == NodeStatus.RUNNING || node.status() == NodeStatus.COMPLETED)) {
            return GraphStatus.RUNNING;
        }
        return GraphStatus.PENDING;
    }

    private List<List<String>> parallelGroups(List<AgentTaskNode> nodes, List<AgentTaskEdge> edges) {
        Map<String, Integer> indegree = new LinkedHashMap<>();
        Map<String, List<String>> outgoing = new HashMap<>();
        for (AgentTaskNode node : nodes) {
            indegree.put(node.id(), 0);
            outgoing.put(node.id(), new ArrayList<>());
        }
        for (AgentTaskEdge edge : edges) {
            indegree.computeIfPresent(edge.toNodeId(), (key, value) -> value + 1);
            outgoing.computeIfAbsent(edge.fromNodeId(), key -> new ArrayList<>()).add(edge.toNodeId());
        }
        ArrayDeque<String> queue = new ArrayDeque<>();
        indegree.forEach((id, degree) -> { if (degree == 0) queue.add(id); });
        List<List<String>> groups = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            List<String> group = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String id = queue.removeFirst();
                if (!seen.add(id)) {
                    continue;
                }
                group.add(id);
                for (String next : outgoing.getOrDefault(id, List.of())) {
                    indegree.computeIfPresent(next, (key, value) -> value - 1);
                    if (indegree.getOrDefault(next, 0) == 0) {
                        queue.add(next);
                    }
                }
            }
            if (!group.isEmpty()) {
                groups.add(group);
            }
        }
        return groups;
    }
}
