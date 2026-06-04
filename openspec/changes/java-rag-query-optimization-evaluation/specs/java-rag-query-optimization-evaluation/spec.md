## ADDED Requirements

### Requirement: Java RAG query optimization
The system SHALL optimize complex retrieval queries using bounded Java/Spring AI stages for coreference resolution, Step-Back abstraction, decomposition, and multi-query rewriting.

#### Scenario: Decompose complex query
- **WHEN** a query exceeds complexity thresholds
- **THEN** the system generates bounded sub-queries and records the optimization strategy used

### Requirement: Traceable query optimization pipeline
The system SHALL execute query optimization through a traceable Java pipeline covering query classification, coreference resolution, Step-Back abstraction, decomposition, multi-query rewriting, hybrid retrieval fan-out, result merge, rerank, MMR diversity, and context packing.

#### Scenario: Preview optimization trace
- **WHEN** a query optimization preview is requested
- **THEN** the system returns the selected strategy, stage outputs, changed queries, fallback reasons, latency, and token cost estimates

### Requirement: Strategy controls
The system SHALL allow each optimization stage or strategy to be enabled, disabled, budgeted, traced, and rolled out through states such as disabled, shadow, canary, enabled, and rollback.

#### Scenario: Run in shadow mode
- **WHEN** a strategy is configured as shadow
- **THEN** the system records optimization traces and evaluation data without changing user-visible retrieval results

### Requirement: Java rerank and diversity stages
The system SHALL support Java rerank and MMR diversity hooks after initial retrieval.

#### Scenario: Rerank retrieved results
- **WHEN** initial retrieval returns candidate chunks
- **THEN** the system reranks and diversifies results before returning context to the Agent

### Requirement: Java-native RAG evaluation
The system SHALL support Java-native evaluation datasets and regression metrics for retrieval quality.

#### Scenario: Run retrieval evaluation
- **WHEN** an evaluation job is triggered
- **THEN** the system computes hit@k, recall@k, MRR, citation coverage, empty retrieval rate, latency, query expansion count, and context token cost metrics

### Requirement: Baseline comparison
The system SHALL compare optimized retrieval against baseline retrieval for each evaluation run.

#### Scenario: Compare optimized retrieval
- **WHEN** an optimized retrieval evaluation run completes
- **THEN** the system stores baseline metrics, optimized metrics, per-scenario deltas, and the selected optimization strategy

### Requirement: Retrieval regression gates
The system SHALL prevent an optimization strategy from becoming the default when configured retrieval quality or cost gates fail.

#### Scenario: Reject degraded strategy
- **WHEN** an optimization strategy lowers recall@k, lowers MRR, lowers citation coverage, increases empty retrieval rate beyond threshold, or exceeds latency/token budgets
- **THEN** the system marks the strategy gate as failed and keeps the previous default strategy active
