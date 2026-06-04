package com.chlobot.platform.app.memory;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MemoryService {

    private final MemoryRepository repository;
    private final MemoryPromptAssembler promptAssembler;

    public MemoryService(MemoryRepository repository, MemoryPromptAssembler promptAssembler) {
        this.repository = repository;
        this.promptAssembler = promptAssembler;
    }

    public AgentMemory create(AgentMemory request) {
        Instant now = Instant.now();
        AgentMemory memory = new AgentMemory(UUID.randomUUID().toString(), request.memoryType(), request.content(),
                request.source(), request.confidence() == null ? 1.0 : request.confidence(),
                request.metadata() == null ? Map.of() : request.metadata(), now, now, false);
        return repository.create(memory);
    }

    public List<AgentMemory> search(String query, String type) {
        return repository.search(query, type);
    }

    public MemoryPromptContext assemblePromptContext(String input, int tokenBudget) {
        List<AgentMemory> confirmed = repository.search(null, null).stream()
                .filter(memory -> memory.confidence() == null || memory.confidence() >= 0.8)
                .toList();
        return promptAssembler.assemble(input, confirmed, tokenBudget);
    }

    public AgentMemory update(String id, AgentMemory update) {
        AgentMemory current = repository.findById(id).orElseThrow();
        AgentMemory merged = new AgentMemory(id,
                update.memoryType() == null ? current.memoryType() : update.memoryType(),
                update.content() == null ? current.content() : update.content(),
                update.source() == null ? current.source() : update.source(),
                update.confidence() == null ? current.confidence() : update.confidence(),
                update.metadata() == null ? current.metadata() : update.metadata(),
                current.createdAt(), Instant.now(), current.deleted());
        repository.update(merged);
        return merged;
    }

    public void delete(String id) {
        repository.delete(id);
    }
}
