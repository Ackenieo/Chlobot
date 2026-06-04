package com.chlobot.platform.app.memory;

import java.util.List;
import java.util.Map;

public record MemoryPromptContext(List<AgentMemory> memories, String systemPromptSection,
                                  int budgetTokens, int usedTokens, int droppedMemories,
                                  Map<String, Object> metadata) {
}
