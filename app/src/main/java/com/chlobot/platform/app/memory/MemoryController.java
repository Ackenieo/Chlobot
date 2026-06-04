package com.chlobot.platform.app.memory;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Operation(summary = "Create memory")
    @PostMapping
    public AgentMemory create(@RequestBody AgentMemory request) {
        return memoryService.create(request);
    }

    @Operation(summary = "Search memories")
    @GetMapping
    public List<AgentMemory> search(@RequestParam(required = false) String query,
                                    @RequestParam(required = false) String type) {
        return memoryService.search(query, type);
    }

    @Operation(summary = "Assemble confirmed memories into a Spring AI prompt context with token budget")
    @GetMapping("/prompt-context")
    public MemoryPromptContext promptContext(@RequestParam(required = false) String input,
                                             @RequestParam(defaultValue = "512") int tokenBudget) {
        return memoryService.assemblePromptContext(input == null ? "" : input, tokenBudget);
    }

    @Operation(summary = "Patch memory")
    @PatchMapping("/{memoryId}")
    public AgentMemory update(@PathVariable String memoryId, @RequestBody AgentMemory request) {
        return memoryService.update(memoryId, request);
    }

    @Operation(summary = "Delete memory")
    @DeleteMapping("/{memoryId}")
    public Map<String, Object> delete(@PathVariable String memoryId) {
        memoryService.delete(memoryId);
        return Map.of("memoryId", memoryId, "deleted", true);
    }
}
