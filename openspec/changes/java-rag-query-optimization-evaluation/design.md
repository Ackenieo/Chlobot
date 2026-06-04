## Context

This follow-up preserves the project highlight of better retrieval quality, but implements it with Java/Spring AI components only.

## Goals / Non-Goals

**Goals:**
- Query classification, rewrite, decomposition, retrieval, rerank, MMR, and context packing in a bounded Java pipeline.
- Coreference resolution, Step-Back abstraction, problem decomposition, and multi-query rewriting through Spring AI-compatible Java stages.
- Rerank/MMR hooks in Java.
- Strategy controls for enabling/disabling stages, per-stage budgets, trace output, fallback behavior, and baseline comparison.
- Offline retrieval evaluation using Java-native datasets and metrics.
- Regression gates that prevent degraded recall/precision/latency strategies from becoming default.

**Non-Goals:**
- No RAGAS runtime dependency.
- No Python evaluation pipeline as part of the MVP runtime.

## Decisions

### 1. Spring AI first

Query rewrites and retrieval hooks should align with Spring AI prompting, advisors, embeddings, and vector store use.

### 2. Java-native evaluation

Metrics such as hit@k, recall@k, MRR, citation coverage, empty retrieval rate, and latency are computed in Java.

### 3. Keep the pipeline bounded

Rewrite and decomposition must have strict step limits.

### 4. Explicit optimization pipeline

The optimizer should execute a traceable, configurable pipeline:

```text
Raw Query
  -> Query Classification
  -> Coreference Resolution
  -> Step-Back Abstraction
  -> Query Decomposition
  -> Multi-Query Rewrite
  -> Hybrid Retrieval Fan-out
  -> Result Merge
  -> Rerank
  -> MMR Diversity
  -> Context Pack with Citations
```

Each stage must record input, output, token cost estimate, latency, fallback reason, and whether it changed the query plan.

### 5. Strategy controls and rollout

Each optimization strategy should support these states:

```text
DISABLED -> SHADOW -> CANARY -> ENABLED -> ROLLBACK
```

Strategies can be enabled per scenario tag, query type, tenant/profile, collection, or model channel. Shadow mode runs optimization and evaluation traces without affecting user-visible retrieval.

### 6. Regression gates

A strategy cannot become default unless evaluation shows acceptable quality and cost:

- recall@k and MRR must not regress against baseline.
- citation coverage must not regress.
- empty retrieval rate must not increase beyond threshold.
- latency and context token cost must stay within configured budgets.
- per-scenario deltas must be visible, not only aggregate metrics.

## API Design

- `POST /rag/evaluations`
- `GET /rag/evaluations/{evaluationId}`
- `POST /rag/query-optimization`

## Data Model

- `rag_evaluation_case`
- `rag_evaluation_run`
- `rag_query_optimization_run`
