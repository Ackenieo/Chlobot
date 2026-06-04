package com.chlobot.platform.app.graph;

import java.util.List;

public record AgentTaskGraph(String taskId, GraphStatus status, List<AgentTaskNode> nodes,
                             List<AgentTaskEdge> edges, List<List<String>> parallelGroups) {
}
