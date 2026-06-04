# Chlobot Frontend API Contract

This document is the frontend-facing API handoff for the Java/Spring Boot MVP. The generated machine-readable schema remains available from Springdoc.

## Global Contract

- Local base URL: `http://localhost:8136/api`
- Swagger UI: `GET /api/swagger-ui.html`
- OpenAPI JSON: `GET /api/v3/api-docs`
- Health: `GET /api/actuator/health`
- Prometheus: `GET /api/actuator/prometheus`
- JSON timestamps are ISO-8601 strings.
- Generic error body from `GlobalExceptionHandler`:

```json
{ "success": false, "message": "error message" }
```

SSE endpoints use `text/event-stream`. Task event SSE ids are `sequenceNo`; event names are lower-case enum values.

## DTO Reference

```ts
type JsonMap = Record<string, unknown>

type TaskStatus =
  | 'PENDING' | 'ANALYZING' | 'PENDING_CLARIFICATION' | 'PLANNING'
  | 'PENDING_CONFIRMATION' | 'CONFIRMED' | 'EXECUTING'
  | 'WAITING_USER_INPUT' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

type NodeStatus = 'PENDING' | 'READY' | 'CLAIMED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SKIPPED'
type GraphStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'

interface AgentSession { id: string; title: string; metadata: JsonMap; createdAt: string; updatedAt: string }
interface CreateSessionRequest { title?: string; metadata?: JsonMap }
interface ConversationMessage { id: number; sessionId: string; role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'; content: string; metadata: JsonMap; createdAt: string }
interface CreateMessageRequest { role: string; content: string; metadata?: JsonMap }

interface AgentTask { id: string; sessionId: string; input: string; mode?: string; status: TaskStatus; riskLevel?: string; budget: JsonMap; result: JsonMap; errorCode?: string; errorMessage?: string; createdAt: string; updatedAt: string }
interface CreateAgentTaskRequest { sessionId: string; input: string; mode?: string; requiresPlanConfirmation?: boolean; budget?: JsonMap }
interface CreateAgentTaskResponse { taskId: string; status: TaskStatus; streamUrl: string; task: JsonMap; createdAt: string }
interface AgentTaskEvent { id?: number; taskId: string; eventType: string; sequenceNo: number; payload: JsonMap; createdAt: string }

interface ChatRequest { message: string; system?: string }
interface ChatResponse { content: string; provider: string; model: string; mock: boolean; intent?: { intent: string; summary: string } | null }
interface ModelChannel { name: string; provider: string; model: string; resourceName: string; priority: number; fallback: boolean }

interface AgentMemory { id?: string; memoryType: string; content: string; source?: string; confidence?: number; metadata?: JsonMap; createdAt?: string; updatedAt?: string; deleted?: boolean }
interface MemoryPromptContext { memories: AgentMemory[]; systemPromptSection: string; budgetTokens: number; usedTokens: number; droppedMemories: number; metadata: JsonMap }

interface IngestDocumentRequest { title: string; content: string; sourceUri?: string; metadata?: JsonMap }
interface DocumentEmbedding { id: number; documentId: string; chunkId: string; title: string; content: string; embedding: unknown; metadata: JsonMap; createdAt: string; updatedAt: string }
interface RagSearchRequest { query: string; topK?: number; mode?: 'vector' | 'keyword' | 'hybrid' | string }
interface RagSearchResult { documentId: string; chunkId: string; title: string; content: string; score: number; metadata: JsonMap }

interface GraphWriteRequest { taskId: string; nodes: NodeSpec[]; edges: EdgeSpec[] }
interface NodeSpec { id: string; name: string; nodeType: string; priority?: number; payload?: JsonMap }
interface EdgeSpec { fromNodeId: string; toNodeId: string; edgeType?: string }
interface AgentTaskNode { id: string; taskId: string; name: string; nodeType: string; status: NodeStatus; owner?: string; priority: number; payload: JsonMap; result: JsonMap; errorMessage?: string; createdAt: string; updatedAt: string }
interface AgentTaskEdge { id: number; taskId: string; fromNodeId: string; toNodeId: string; edgeType: string; createdAt: string }
interface AgentTaskGraph { taskId: string; status: GraphStatus; nodes: AgentTaskNode[]; edges: AgentTaskEdge[]; parallelGroups: string[][] }
```

## Sessions and Messages

### `POST /sessions`

Create a session.

```bash
curl -sS -X POST http://localhost:8136/api/sessions \
  -H 'Content-Type: application/json' \
  -d '{"title":"Frontend smoke","metadata":{"source":"ui"}}'
```

Request: `CreateSessionRequest`. Response: `AgentSession`.

### `GET /sessions?page=0&size=20`

List non-deleted sessions. Response: `AgentSession[]`.

### `GET /sessions/{sessionId}`

Read one session. Response: `AgentSession`.

### `POST /sessions/{sessionId}/messages`

Add a message.

```json
{ "role": "USER", "content": "hello", "metadata": { "ui": true } }
```

Response: `ConversationMessage`.

### `GET /sessions/{sessionId}/messages`

List messages in creation order. Response: `ConversationMessage[]`.

## Agent Tasks

### `POST /agent/tasks`

Create and start an Agent task.

```bash
curl -sS -X POST http://localhost:8136/api/agent/tasks \
  -H 'Content-Type: application/json' \
  -d '{"sessionId":"<sessionId>","input":"draft a plan","mode":"CHAT","requiresPlanConfirmation":false,"budget":{"maxTokens":2048}}'
```

Request: `CreateAgentTaskRequest`. Response: `CreateAgentTaskResponse`.

### `GET /agent/tasks/{taskId}`

Response: `AgentTask`.

### `GET /agent/tasks/{taskId}/events`

SSE replay of `AgentTaskEvent`. Use `EventSource('/api/agent/tasks/{taskId}/events')`.

### `GET /agent/tasks/{taskId}/timeline`

REST replay of events. Response: `AgentTaskEvent[]`.

### `POST /agent/tasks/{taskId}/confirm-plan`

Confirm generated plan.

```json
{ "approved": true, "notes": "OK" }
```

Response: map with task status/decision details.

### `POST /agent/tasks/{taskId}/cancel`

Cancel task. Response: `JsonMap`.

### `POST /agent/tasks/{taskId}/retry`

Retry failed/cancelled task path according to Java policy. Response: `JsonMap`.

### `GET /agent/tasks/{taskId}/result`

Response:

```json
{ "taskId": "...", "status": "COMPLETED", "result": {} }
```

## Chat and Model Channels

### `POST /chat`

```bash
curl -sS -X POST http://localhost:8136/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"hello","system":"You are concise"}'
```

Request: `ChatRequest`. Response: `ChatResponse`.

### `POST /chat/stream`

SSE/Flux stream of `ChatResponse` chunks.

### `POST /chat/structured-intent`

Parse model output into a validated intent response. Request: `ChatRequest`. Response: `ChatResponse` with `intent`.

### `GET /model/channels`

Response:

```ts
interface ModelChannelHealth {
  active: string
  sentinelResource: string
  retryPolicy: string
  fallbackEnabled: boolean
  channels: ModelChannel[]
}
```

## Tool Governance

### `GET /agent/tasks/{taskId}/tool-calls`

List tool calls for a task. Response shape follows `AgentToolCall` Java record/table mapping.

### `POST /agent/tasks/{taskId}/tool-calls/{toolCallId}/confirm`

```json
{ "approved": true, "reason": "safe read-only action" }
```

Response: confirmation result map.

### `GET /agent/tools/spring-ai/plan-ack`

Returns a Spring AI-compatible governed tool descriptor:

```ts
interface SpringAiToolDescriptor {
  name: string
  description: string
  inputSchema: string
  permissionLevel: string
  readOnly: boolean
  requiresConfirmation: boolean
  timeoutSeconds: number
  maxRetries: number
  idempotent: boolean
  springAiCompatible: boolean
  governance: JsonMap
}
```

## Memories

### `POST /memories`

Create memory. Request/response: `AgentMemory`.

### `GET /memories?query=&type=`

Search active memories. Response: `AgentMemory[]`.

### `GET /memories/prompt-context?input=...&tokenBudget=512`

Preview Spring AI prompt context assembled from confirmed memories. Response: `MemoryPromptContext`.

### `PATCH /memories/{memoryId}`

Patch memory. Request/response: `AgentMemory`.

### `DELETE /memories/{memoryId}`

Response:

```json
{ "memoryId": "...", "deleted": true }
```

## RAG

### `POST /rag/documents`

Ingest one document/chunk. Request: `IngestDocumentRequest`. Response: `DocumentEmbedding`.

### `POST /rag/search`

Search documents.

```json
{ "query": "Spring AI", "topK": 5, "mode": "hybrid" }
```

Response: `RagSearchResult[]`.

### `GET /rag/documents/{documentId}`

Current MVP returns the document id string. Treat as placeholder until detail retrieval is expanded.

### `GET /rag/evaluation?query=...`

Returns Java-native RAG quality metrics map, including hit@k/recall/MRR/citation/empty retrieval/latency fields where available.

## DAG Graphs

### `POST /agent/graphs`

Write a task-scoped DAG.

```json
{
  "taskId": "<taskId>",
  "nodes": [
    { "id": "backend", "name": "Backend", "nodeType": "work", "priority": 10, "payload": {} },
    { "id": "frontend", "name": "Frontend", "nodeType": "work", "priority": 10, "payload": {} },
    { "id": "verify", "name": "Verify", "nodeType": "gate", "priority": 0, "payload": {} }
  ],
  "edges": [
    { "fromNodeId": "backend", "toNodeId": "verify", "edgeType": "BLOCKS" },
    { "fromNodeId": "frontend", "toNodeId": "verify", "edgeType": "BLOCKS" }
  ]
}
```

Response: `AgentTaskGraph`.

### `GET /agent/graphs/{taskId}`

Read graph. Response: `AgentTaskGraph`.

### `GET /agent/graphs/{taskId}/ascii`

Text visualization.

### `GET /agent/graphs/{taskId}/ready-nodes`

Response: `AgentTaskNode[]`.

### `POST /agent/graphs/{taskId}/nodes/{nodeId}/claim`

```json
{ "owner": "frontend-user" }
```

Response: `AgentTaskNode`.

### `POST /agent/graphs/{taskId}/nodes/{nodeId}/complete`

Request: result map. Response: `AgentTaskNode`.

### `POST /agent/graphs/{taskId}/nodes/{nodeId}/fail`

```json
{ "errorMessage": "reason" }
```

Response: `AgentTaskNode`.

### `POST /agent/graphs/{taskId}/execute?concurrency=2`

Execute ready DAG nodes through Java executor. Response: completed `AgentTaskNode[]`.

## Observability and Ops

### `GET /observability/metrics/summary`

Response:

```ts
interface ObservabilitySummary {
  taskCount: number
  sessionCount: number
  status: 'ok'
  model: { calls: number; retries: number; fallbacks: number; estimatedTokens: number }
  rag: { hitAt5: number; recallAt5: number; mrr: number; citationCoverage: number; emptyRetrievalRate: number }
  tools: { pendingConfirmations: number; approvals: number; rejections: number; riskyCalls: number }
  memory: { confirmedFacts: number; selected: number; dropped: number; ruleConflicts: number }
  contextBudget: { windowTokens: number; outputReserve: number; usedTokens: number; droppedBlocks: number }
}
```

### `GET /observability/tasks/{taskId}/timeline`

Response: `AgentTaskEvent[]`.

### `GET /observability/tasks/{taskId}/trace`

Response:

```ts
interface AgentExecutionTrace {
  taskId: string
  timeline: AgentTaskEvent[]
  dagNodes: AgentTaskNode[]
  modelCalls: unknown[]
  toolCalls: unknown[]
  ragRetrievals: unknown[]
  memoryAdvisorInjections: unknown[]
  retries: unknown[]
  errors: unknown[]
}
```

### `GET /actuator/health`

Spring Boot health with MySQL and PostgreSQL details.

### `GET /actuator/prometheus`

Prometheus text format. Frontend should display selected model metrics as text snippets unless a metrics API is later added.

## Frontend Coverage Matrix

| Route | Backing APIs | Implementation status |
| --- | --- | --- |
| `/` / `#/dashboard` | health, metrics summary, model channels | Implement now |
| `#/sessions` | sessions/messages | Implement now |
| `#/tasks` | agent tasks, timeline, result, SSE | Implement now |
| `#/rag` | RAG ingest/search/evaluation | Implement now |
| `#/memories` | memory CRUD, prompt context | Implement now |
| `#/graphs` | graph write/read/execute/ready/ascii | Implement now |
| `#/observability` | metrics summary, task trace, Prometheus | Implement now |
| `#/model-channels` | model channels, chat smoke, metrics | Implement now |
| `#/tool-risk` | tool calls, confirm, descriptor | Implement now |
| `#/observability/research` | future research workflow APIs | Placeholder until `java-research-workflow` |
| `#/observability/context-budget` | later context budget APIs | Placeholder until `java-bootstrap-context-loading` |
| Swarm/Supervisor control | future `/multi-agent/*` APIs | Placeholder until `java-multiagent-orchestration` |
