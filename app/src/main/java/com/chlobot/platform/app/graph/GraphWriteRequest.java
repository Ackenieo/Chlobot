package com.chlobot.platform.app.graph;

import java.util.List;
import java.util.Map;

public record GraphWriteRequest(String taskId, List<NodeSpec> nodes, List<EdgeSpec> edges) {

    public record NodeSpec(String id, String name, String nodeType, int priority, Map<String, Object> payload) {
    }

    public record EdgeSpec(String fromNodeId, String toNodeId, String edgeType) {
    }
}
