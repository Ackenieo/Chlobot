## ADDED Requirements

### Requirement: Spring AI context assembly
The system SHALL assemble model context through Java/Spring AI Prompt or Advisor components with explicit section budgets.

#### Scenario: Build context within budget
- **WHEN** context preview is requested with a token budget
- **THEN** the system returns selected context blocks and excludes low-priority content that would exceed the budget

### Requirement: Java context section model
The system SHALL use implementation-neutral context sections: `SYSTEM`, `USER_INPUT`, `RECENT_MESSAGES`, `MEMORY`, `RAG_CONTEXT`, `TOOL_RESULTS`, `BEHAVIOR_RULES`, and `OUTPUT_RESERVE`.

#### Scenario: Inspect selected sections
- **WHEN** context preview is returned
- **THEN** each selected block includes its section, priority, estimated tokens, and source

### Requirement: Context budget allocator
The system SHALL allocate token budgets through an explicit Java `ContextBudgetAllocator` that reserves output tokens, applies safety margin, and assigns per-section budgets.

#### Scenario: Allocate budgets for a deep task
- **WHEN** a task requests a large context window
- **THEN** the allocator returns section budgets that fit within the model window while preserving output reserve and safety margin

### Requirement: Context block scoring and packing
The system SHALL score candidate context blocks using relevance, freshness, authority, user preference, task need, recency, and token cost before packing them into the prompt.

#### Scenario: Rank candidate blocks
- **WHEN** multiple candidate blocks compete for the same budget
- **THEN** the system keeps the highest scoring blocks and records why lower scoring blocks were dropped or compressed

### Requirement: Overflow protection
The system SHALL protect the model window by preventing output reserve overrun, applying section-level truncation, and falling back to summarization when needed.

#### Scenario: Prevent overflow
- **WHEN** selected blocks would exceed the model window
- **THEN** the system truncates or summarizes low-priority blocks, preserves citations/source references where possible, and records overflow prevention metadata

### Requirement: Context budget preview and metrics
The system SHALL expose preview and metrics for budget usage, selected blocks, dropped blocks, compressed blocks, cache hits, and overflow prevention events.

#### Scenario: View budget preview
- **WHEN** a user requests context preview or budget metrics
- **THEN** the system returns model window usage, section budgets, selected blocks, dropped blocks, compressed blocks, and cache hit/miss metrics

### Requirement: Optional Java cache support
The system SHALL support optional Caffeine L1 caching for reusable context fragments and MAY add Spring Data Redis L2 after measurable hot-path need.

#### Scenario: Context cache hit
- **WHEN** a reusable context fragment exists in an enabled cache
- **THEN** the system uses the cached fragment and records cache hit metrics
