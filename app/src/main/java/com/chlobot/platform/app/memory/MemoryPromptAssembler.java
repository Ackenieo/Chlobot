package com.chlobot.platform.app.memory;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MemoryPromptAssembler {

    public MemoryPromptContext assemble(String userInput, List<AgentMemory> confirmedMemories, int tokenBudget) {
        List<AgentMemory> selected = new ArrayList<>();
        int used = 0;
        int budget = Math.max(tokenBudget, 32);
        for (AgentMemory memory : confirmedMemories) {
            int tokens = estimateTokens(memory.content());
            if (used + tokens > budget) {
                break;
            }
            selected.add(memory);
            used += tokens;
        }
        selected.sort(Comparator.comparing(AgentMemory::updatedAt).reversed());
        int dropped = Math.max(0, confirmedMemories.size() - selected.size());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("userInput", userInput);
        metadata.put("selectedMemoryIds", selected.stream().map(AgentMemory::id).toList());
        metadata.put("droppedMemoryCount", dropped);
        metadata.put("tokenBudget", budget);
        return new MemoryPromptContext(selected,
                buildSystemSection(selected),
                budget,
                used,
                dropped,
                metadata);
    }

    private String buildSystemSection(List<AgentMemory> memories) {
        if (memories.isEmpty()) {
            return "No confirmed memories available.";
        }
        StringBuilder builder = new StringBuilder("Confirmed memories:\n");
        for (AgentMemory memory : memories) {
            builder.append("- ").append(memory.memoryType()).append(": ").append(memory.content()).append('\n');
        }
        return builder.toString();
    }

    private int estimateTokens(String value) {
        if (value == null || value.isBlank()) {
            return 4;
        }
        return Math.max(4, value.getBytes(StandardCharsets.UTF_8).length / 4 + 1);
    }
}
