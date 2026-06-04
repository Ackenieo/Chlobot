## Why

The original plan includes query optimization and evaluation for RAG. The Java follow-up should keep the capability, but implement it with Java/Spring AI and Java-native metrics rather than Python-first evaluation libraries.

## What Changes

- Define a bounded Java query optimization pipeline: raw query classification, coreference resolution, Step-Back abstraction, decomposition, multi-query rewrite, hybrid retrieval, rerank, MMR diversity, and context packing.
- Add Spring AI based retrieval optimization hooks for coreference, Step-Back, decomposition, and multi-query rewrites.
- Add strategy controls so each optimization stage can be enabled, disabled, budgeted, traced, and compared against baseline retrieval.
- Add rerank/MMR integration points that stay inside the Java service boundary.
- Implement Java-native retrieval evaluation datasets and metrics.
- Add regression gates that compare optimized retrieval against baseline hit@k, recall@k, MRR, citation coverage, empty retrieval rate, and latency before enabling a new strategy.

## Capabilities

- `java-rag-query-optimization-evaluation`: Java/Spring AI query optimization and retrieval evaluation.

## Impact

- Code structure: query optimizer, evaluator, datasets, metrics.
- API: evaluation job launch and result query endpoints.
- Dependency: Spring AI, Java search/retrieval libraries, Micrometer.
