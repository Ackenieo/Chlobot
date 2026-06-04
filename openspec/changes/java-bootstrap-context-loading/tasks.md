## 1. Spring AI Context Assembly

- [ ] 1.1 Define `ContextBudgetAllocator` for model window, output reserve, system, user input, recent messages, memory, RAG, tool results, behavior rules, and safety margin
- [ ] 1.2 Implement Spring AI Prompt / Advisor based context assembly service
- [ ] 1.3 Add context block model with section, source, priority, estimated tokens, score, selected/dropped/compressed status, and citation metadata
- [ ] 1.4 Add scoring formula using relevance, freshness, authority, userPreference, taskNeed, recency, and tokenCost
- [ ] 1.5 Add deduplication, context packing, section-level truncation, fallback summarization, and hard max token overflow protection
- [ ] 1.6 Add preview endpoint for assembled context blocks and dropped/compressed block reasons
- [ ] 1.7 Add metrics for budget usage, selected blocks, dropped blocks, compressed blocks, overflow prevention, and cache hits

## 2. Java Cache Support

- [ ] 2.1 Add optional Caffeine L1 cache for reusable context fragments
- [ ] 2.2 Add Redis L2 only if a measured hot path justifies it
- [ ] 2.3 Keep cache policy configurable by profile, section, source type, TTL, and invalidation event
- [ ] 2.4 Emit cache hit/miss/eviction metrics tagged by section and source type

## 3. Memory, RAG, Tool, and Rule Integration

- [ ] 3.1 Feed confirmed memories into context assembly
- [ ] 3.2 Feed selected retrieval results into context assembly with citations preserved
- [ ] 3.3 Feed compacted tool results into context assembly under tool result budget
- [ ] 3.4 Feed active behavior rules and pitfall memories into context assembly under rule budget
- [ ] 3.5 Verify context assembly stays within token budget by tests

## 4. Frontend and Observability

- [ ] 4.1 Add `/observability/context-budget` or `/bootstrap/context-budget` API for budget visualization
- [ ] 4.2 Visualize model window, used tokens, output reserve, section budgets, selected blocks, dropped blocks, compressed blocks, and overflow prevention events
- [ ] 4.3 Add curl verification for context preview and context budget visualization endpoints
