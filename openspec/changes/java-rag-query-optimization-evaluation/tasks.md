## 1. Query Optimization Pipeline

- [ ] 1.1 Add bounded Java optimization pipeline: classify -> coreference -> Step-Back -> decompose -> multi-query rewrite -> hybrid retrieval -> rerank -> MMR -> context pack
- [ ] 1.2 Add query classification for simple, conversational, comparative, multi-hop, ambiguous, and long-context queries
- [ ] 1.3 Add coreference resolution stage for conversational references and omitted entities
- [ ] 1.4 Add Step-Back abstraction prompts through Spring AI-compatible Java services
- [ ] 1.5 Add decomposition stage for multi-hop or compound questions with strict max sub-query limits
- [ ] 1.6 Add multi-query rewrite stage with deduplication and query budget controls
- [ ] 1.7 Add hybrid retrieval fan-out and result merge strategy
- [ ] 1.8 Add rerank/MMR hooks and context packing with citation preservation
- [ ] 1.9 Persist optimization trace, selected strategy, stage outputs, and fallback reasons
- [ ] 1.10 Verify optimization strategies with unit tests

## 2. Evaluation and Regression Gates

- [ ] 2.1 Define labeled retrieval datasets with query, relevant document/chunk ids, expected citations, and scenario tags
- [ ] 2.2 Compute Java-native evaluation metrics: hit@k, recall@k, MRR, citation coverage, empty retrieval rate, latency, query expansion count, and context token cost
- [ ] 2.3 Persist evaluation runs, baseline metrics, optimized metrics, per-scenario deltas, and failed gates
- [ ] 2.4 Add baseline-vs-optimized comparison for every strategy change
- [ ] 2.5 Add regression gates for minimum recall@k/MRR improvement, maximum latency increase, maximum token cost increase, and no citation coverage degradation
- [ ] 2.6 Add strategy rollout status: disabled, shadow, canary, enabled, rollback

## 3. API and Verification

- [ ] 3.1 Add query optimization preview endpoint that returns optimized queries, selected strategy, and trace
- [ ] 3.2 Add evaluation run endpoint with baseline comparison and regression gate result
- [ ] 3.3 Verify query optimization endpoint with curl
- [ ] 3.4 Verify evaluation run output, baseline comparison, gate result, and metrics storage
