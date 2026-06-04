## 1. Backend Foundation

- [x] 1.1 Create root Maven `pom.xml` and module `pom.xml` files for `common`, `agent-core`, `agent-tools`, `agent-memory`, `agent-rag`, `agent-model`, `agent-sandbox`, and `app`
- [x] 1.2 Add Spring Boot 3, Java 21, validation, actuator, OpenAPI, persistence, database driver, and test dependencies
- [x] 1.3 Create Spring Boot application entrypoint under `app` and configuration files for local/docker profiles
- [x] 1.4 Align environment variable names for DeepSeek/OpenAI-compatible config and database config
- [x] 1.5 Ensure Dockerfile can build the Maven multi-module application
- [x] 1.6 Audit dependencies and remove Python-first/cross-ecosystem Agent framework assumptions from the Java MVP plan

## 2. Spring AI Model Foundation

- [x] 2.1 Use Spring AI `ChatModel` / `StreamingChatModel` as the only model abstraction exposed to `agent-core`
- [x] 2.2 Configure DeepSeek through Spring AI OpenAI-compatible properties instead of direct provider calls from controllers
- [x] 2.3 Implement a Java `ModelClient` adapter around Spring AI for timeout, retry, metrics, streaming callbacks, and mock profile fallback
- [x] 2.4 Implement Spring AI structured output helpers using Java `record` DTOs, Bean Validation, and Jackson
- [x] 2.5 Add curl verification for a mock or real Spring AI chat endpoint after implementation

## 3. Database and Persistence

- [x] 3.1 Expand MySQL initialization/migration SQL for sessions, messages, tasks, plans, events, tool calls, audit logs, and memories
- [x] 3.2 Fix PostgreSQL pgvector initialization SQL to use valid PostgreSQL comments and required indexes
- [x] 3.3 Implement entity/model classes and repository/DAO layer for MySQL business tables
- [x] 3.4 Implement DAO layer for PostgreSQL document embedding search and document metadata
- [x] 3.5 Add basic repository tests or startup validation for both databases

## 4. Agent Core and Task Orchestration

- [x] 4.1 Implement task status enum, event type enum, and state transition validator
- [x] 4.2 Implement task creation service that persists task, initial event, and budget metadata
- [x] 4.3 Implement plan generation placeholder and plan confirmation flow
- [x] 4.4 Implement mock execution flow that emits progress, partial result, completed, failed, and cancelled events
- [x] 4.5 Implement retry and cancel policies with persisted events
- [x] 4.6 Implement SSE event publisher backed by persisted event replay
- [x] 4.7 Route future LLM planning/execution through Spring AI adapters, not provider SDKs or non-Java Agent frameworks

## 5. Session and Message API

- [x] 5.1 Implement session create/list/get endpoints
- [x] 5.2 Implement message create/list endpoints under sessions
- [x] 5.3 Preserve message roles, metadata, timestamps, and soft-delete filtering
- [x] 5.4 Add OpenAPI descriptions for session and message DTOs

## 6. Agent Task API

- [x] 6.1 Implement `POST /api/agent/tasks`
- [x] 6.2 Implement `GET /api/agent/tasks/{taskId}`
- [x] 6.3 Implement `GET /api/agent/tasks/{taskId}/events` as SSE
- [x] 6.4 Implement `POST /api/agent/tasks/{taskId}/confirm-plan`
- [x] 6.5 Implement `POST /api/agent/tasks/{taskId}/cancel`
- [x] 6.6 Implement `POST /api/agent/tasks/{taskId}/retry`
- [x] 6.7 Implement `GET /api/agent/tasks/{taskId}/result`

## 7. Spring AI Tool Governance

- [x] 7.1 Implement tool metadata model with permission level, read-only flag, schema, timeout, retry, and idempotency fields
- [x] 7.2 Implement tool call persistence and listing by task
- [x] 7.3 Implement confirmation endpoint for pending tool calls
- [x] 7.4 Emit and persist confirmation-required, approved, rejected, started, and completed events
- [x] 7.5 Persist audit log records for confirmation decisions and executed tool calls
- [x] 7.6 Map internal tools to Spring AI Tool Calling descriptors where possible
- [x] 7.7 Keep dangerous tools behind Java permission checks, confirmation flow, idempotency policy, timeout, and audit logging

## 8. Memory Management

- [x] 8.1 Implement memory create endpoint
- [x] 8.2 Implement memory query endpoint with type/query pagination filters
- [x] 8.3 Implement memory patch endpoint
- [x] 8.4 Implement memory delete endpoint
- [x] 8.5 Ensure deleted memories are excluded from normal retrieval
- [x] 8.6 Integrate confirmed memories into Spring AI Prompt/Advisor context assembly with explicit token budget

## 9. Spring AI RAG Knowledge Retrieval

- [x] 9.1 Implement document ingestion endpoint for title/content/source/metadata
- [x] 9.2 Implement deterministic chunking placeholder and embedding placeholder or mock vector storage path
- [x] 9.3 Implement vector/keyword/hybrid search request DTO and response DTO with provenance
- [x] 9.4 Implement PostgreSQL-backed retrieval for available document embeddings or fallback keyword search
- [x] 9.5 Add OpenAPI examples for document ingestion and search
- [x] 9.6 Use Spring AI `EmbeddingModel` for embeddings and Spring AI `VectorStore` abstraction for pgvector where compatible
- [x] 9.7 Keep keyword/hybrid search Java-native using PostgreSQL full-text, JDBC/MyBatis/JPA, or Lucene-compatible Java library if needed
- [x] 9.8 Implement Java-native RAG evaluation metrics: hit@k, recall@k, MRR, citation coverage, empty retrieval rate, and latency

## 10. Observability and API Documentation

- [x] 10.1 Implement task timeline endpoint backed by persisted events
- [x] 10.2 Implement metrics summary endpoint with task counts and basic latency/status indicators
- [x] 10.3 Enable actuator health under `/api/actuator/health`
- [x] 10.4 Enable Swagger UI at `/api/swagger-ui.html`
- [x] 10.5 Document all public REST/SSE endpoints and event payload shapes in OpenAPI annotations or docs
- [x] 10.6 Add Micrometer Prometheus registry and expose `/api/actuator/prometheus`
- [x] 10.7 Add Grafana dashboard JSON or documented panel plan for Java service metrics
- [x] 10.8 Add frontend `/observability` dashboard with service health, QPS/error/latency cards, task timeline, DAG trace, model metrics, RAG quality, tool risk, memory/rule hits, and recent incidents
- [x] 10.9 Add Agent execution trace API/view covering state transitions, DAG nodes, model calls, tool calls, RAG retrieval, memory Advisor injection, retries, and errors
- [x] 10.10 Add prompt token, completion token, estimated cost, retry, fallback, and channel metrics panels
- [x] 10.11 Add RAG quality dashboard panels for query optimization traces, evaluation runs, baseline comparison, regression gates, citation coverage, empty retrieval rate, and low-quality queries
- [x] 10.12 Add multi-agent/research monitoring panels for roles, Swarm/Supervisor decisions, ToT branches, parallel tracks, reflection checkpoints, artifacts, votes, report stream, and export status
- [x] 10.13 Add memory and behavior-rule monitoring panels for candidate extraction, confirmed profile facts, pitfall matches, Advisor injection trace, rule conflicts, and application audit
- [x] 10.14 Add tool risk dashboard panels for pending confirmations, approvals/rejections, timeouts, retries, idempotency decisions, risky tool frequency, and audit records
- [x] 10.15 Add context budget visualization panels for model window, output reserve, section budgets, selected/dropped/compressed blocks, cache hits, and overflow prevention

## 11. Frontend Parallel Work Package

- [x] 11.1 Define frontend route map: session list, task console, task detail timeline, memory manager, RAG document/search page, observability page, traffic health page, model channel health page, RAG quality page, tool risk page, memory/rule audit page, and multi-agent/research monitor page
- [x] 11.2 Define TypeScript API client types matching backend DTOs and SSE event payloads
- [x] 11.3 Implement mock-backed UI for task creation, plan confirmation, cancel, retry, and timeline rendering
- [x] 11.4 Implement confirmation card UI for risky tool calls
- [x] 11.5 Implement memory CRUD UI
- [x] 11.6 Implement RAG document import and search UI
- [x] 11.7 Switch frontend API client from mocks to backend endpoints when available

## 12. Java Task Graph Orchestration

- [x] 12.1 Extract generic DAG concepts: nodes, edges, blockedBy, ready nodes, claim, complete, fail, unlock, visualize
- [x] 12.2 Add MySQL tables for `agent_task_node`, `agent_task_edge`, and `agent_task_node_event`
- [x] 12.3 Implement Java graph model: `AgentTaskGraph`, `AgentTaskNode`, `AgentTaskEdge`, `NodeStatus`, `GraphStatus`
- [x] 12.4 Implement graph writer that persists nodes/edges and computes topological order plus parallel groups
- [x] 12.5 Implement Java ready-node resolver that returns `PENDING` nodes whose dependencies are `COMPLETED`
- [x] 12.6 Implement transactional atomic node claim and owner assignment using MySQL row update/lock semantics
- [x] 12.7 Implement node complete/fail operations and dependency unlock behavior
- [x] 12.8 Implement graph status and ASCII/JSON visualization in Java
- [x] 12.9 Implement parallel graph executor with `ThreadPoolTaskExecutor` / `CompletableFuture` and configurable concurrency
- [x] 12.10 Emit graph/node events into existing task timeline and SSE stream
- [x] 12.11 Add REST APIs for graph write/read/ready-nodes/execute/claim/complete/fail
- [x] 12.12 Verify with curl using a DAG where backend and frontend nodes run in parallel before verify node

## 13. Java Traffic Health and Model Governance

- [x] 13.1 Add plan for traffic health monitoring console with QPS/error/P95/P99/channel status
- [x] 13.2 Add first-token and first-SSE-event latency monitoring plan
- [x] 13.3 Add Sentinel-based model channel priority, circuit breaker, fallback, and governance plan
- [x] 13.4 Pull Sentinel Dashboard Docker image and add compose service on project-offset port 8914
- [x] 13.5 Implement `ModelChannelRouter` as a Java wrapper over Spring AI `ChatModel`/`StreamingChatModel`
- [x] 13.6 Add Micrometer timers/counters for model calls, first token latency, first SSE latency, retries, fallback usage, and token estimates
- [x] 13.7 Add Sentinel/Spring Cloud Alibaba Sentinel resource naming for model channels
- [x] 13.8 Add Spring Retry or Resilience4j only where Sentinel does not cover the use case

## 14. Java Context, Cache, Research, and MCP Follow-up Plans

- [x] 14.1 Add Spring AI Prompt/Advisor based Bootstrap context assembly with explicit section budgets
- [x] 14.2 Add Java cache plan using Caffeine L1 and Spring Data Redis L2 only after measurable need or clear hot-path usage
- [x] 14.3 Add Spring AI based RAG query optimizer plan: bounded query rewrite, decomposition, Step-Back prompting, Rerank/MMR interface
- [x] 14.4 Add Java-native RAG evaluation plan and explicitly exclude RAGAS from the MVP runtime
- [x] 14.5 Add research workflow plan based on Java task state machine, DAG executor, Spring AI, and SSE; do not depend on non-Java Agent frameworks
- [x] 14.6 Add MCP integration plan only through Java/Spring AI MCP support or a Java HTTP/SSE adapter; all MCP tools must enter existing tool governance
- [x] 14.7 Add onboarding, confirmed user profile extraction, context archive, and restore as later independent Java/Spring changes
- [x] 14.8 Create Java follow-up changes: `java-bootstrap-context-loading`, `java-rag-query-optimization-evaluation`, `java-research-workflow`, `java-research-session-resume`, `java-mcp-integration-management`, `java-onboarding-context-archive`, `java-multiagent-orchestration`, `java-agent-self-evolving-knowledge`


## 15. Verification

- [x] 15.1 Build the Maven project locally or through Docker
- [x] 15.2 Start services with Docker Compose
- [x] 15.3 Use `curl http://localhost:8136/api/actuator/health` to verify health
- [x] 15.4 Use curl to create a session and add/list messages
- [x] 15.5 Use curl to create an Agent task, subscribe or replay events, confirm plan, and fetch result
- [x] 15.6 Use curl to exercise memory create/query/update/delete
- [x] 15.7 Use curl to ingest and search a RAG document
- [x] 15.8 Record any skipped verification with reason and next action
- [x] 15.9 After each new Java/Spring feature, verify with curl according to project rule
