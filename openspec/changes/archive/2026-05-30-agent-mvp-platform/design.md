## Context

仓库当前已具备 `chlobot-compose.yml`、`Dockerfile`、MySQL/PostgreSQL 初始化脚本和 Agent 落地设计哲学文档。项目方向必须收敛为 Java/Spring Boot/Spring AI 生态：MVP 主链路不得依赖 Python-first Agent 框架、跨语言 workflow runtime 或仅在其他语言生态成熟的库。出现库生态选择时，必须优先寻找 Spring AI、Spring Boot、Spring Data、Spring Cloud Alibaba、Micrometer、JDK 21 并发或 Java 产品对标方案。

目标形态是一个可运行的 Chlobot MVP：后端提供稳定 REST + SSE API，前端可并行实现任务控制台；系统以确定性任务状态机约束 Spring AI 模型调用、工具、RAG、记忆和人工确认流程。数据库由 MySQL 承载业务轨迹，PostgreSQL + pgvector 承载知识向量。

建议整体结构：

```text
Frontend / API Client
        │ REST + SSE
        ▼
app ─────────────────────────────────────────────┐
│ Controller / DTO / OpenAPI / Security Stub      │
└──────────────┬──────────────────────────────────┘
               ▼
agent-core ─ Orchestrator / State Machine / Events / DAG Executor
        ├── agent-model  ─ Spring AI ChatModel / StreamingChatModel / Prompt / Structured Output
        ├── agent-tools  ─ Spring AI Tool Calling Adapter / Tool Registry / Confirmation
        ├── agent-memory ─ MySQL Memory / Spring AI Advisor Context
        ├── agent-rag    ─ Spring AI EmbeddingModel / VectorStore / Retriever / Java Evaluation
        └── agent-sandbox─ Future Java-governed command/file sandbox boundary
               ▼
common ─ Shared DTOs / Errors / IDs / Time / Result
               ▼
MySQL: sessions, messages, tasks, plans, events, graph nodes, tool_calls, audits, memories
PostgreSQL: pgvector document embeddings, document chunks, retrieval metadata
```

## Goals / Non-Goals

**Goals:**

- 建立与 Dockerfile 匹配的 Maven 多模块 Spring Boot 3 工程。
- 以 Spring AI 作为模型、Prompt、Embedding、VectorStore、Tool Calling、RAG 和结构化输出主抽象。
- 提供可运行的 API 服务，健康检查地址为 `/api/actuator/health`。
- 提供面向前端的接口文档和 DTO，避免前端依赖内部 Prompt 或 Agent 节点名。
- 用状态机建模 Agent 任务生命周期，并持久化任务事件。
- 支持 SSE 事件流，让前端展示计划、进度、工具调用、错误和完成结果。
- 使用 Java/Spring 实现 DAG 任务图：计划写入、任务依赖、ready node 查询、事务性 claim、依赖解锁、并行组计算、图状态可视化。
- 提供最小可用的工具确认、记忆管理和 RAG 文档/检索接口。
- 使用数据库迁移或初始化脚本补齐业务表，并修正 PostgreSQL 初始化语法。
- 使用 Spring Boot Actuator + Micrometer + Prometheus/Grafana/Sentinel 做 Java 服务观测和治理。
- 形成后端与前端可以并行实现的任务拆分。

**Non-Goals:**

- 不在 MVP 中实现完整多租户、细粒度权限、生产级计费或灰度发布。
- 不在 MVP 中暴露真实危险工具执行能力；危险工具先以确认流和审计模型占位。
- 不强制实现完整前端仓库；本变更提供 API 合约与前端任务，可在后续新增 `web/`。
- 不引入 Python-first Agent 框架、跨语言 workflow runtime、LangChain/LangGraph/LlamaIndex/RAGAS runtime 作为 Java MVP 主链路依赖。
- 不追求复杂多 Agent Swarm；MVP 以单 Java 编排器 + DAG 可并行子任务模型为基础。
- PDF 导出、复杂 onboarding、完整上下文归档恢复先后置为独立 Java/Spring change。

## Decisions

### 1. 使用 Maven 多模块 + Spring Boot 3

选择 Maven 多模块是因为 Dockerfile 已按该结构编写，且 Java 生态中 Spring Boot 3 + JDK 21 是当前最稳妥的服务端落地组合。模块边界与设计哲学对应，能让 Agent 核心、工具、RAG、记忆和模型调用保持低耦合。

备选：单模块 Spring Boot。放弃原因：短期更快，但很快会把 Controller、编排、RAG、工具和记忆混在一起，违背可演进目标。

### 2. Spring AI 是 AI 能力主线

模型调用、流式输出、Embedding、VectorStore、Tool Calling、Prompt、结构化输出和 RAG 优先使用 Spring AI 抽象。DeepSeek 通过 OpenAI-compatible 配置接入 Spring AI OpenAI 兼容能力。

`agent-model` 不直接暴露供应商 SDK 给业务层，而提供围绕 Spring AI 的 Java adapter：

```text
Agent Executor
  └── ModelClient
        ├── SpringAiChatModelAdapter
        ├── SpringAiStreamingChatModelAdapter
        ├── timeout / retry / metrics / fallback
        └── mock profile for local tests
```

备选：直接在 Controller 调 provider SDK。放弃原因：不利于超时、重试、降级、观测、测试和后续模型切换。

### 3. API 采用 REST + SSE，而不是 WebSocket 优先

任务创建、确认、取消、重试等操作使用 REST；长任务过程反馈使用 SSE。SSE 对单向事件流足够简单，浏览器支持好，部署成本低，适合 MVP 的任务时间线展示。

备选：WebSocket。放弃原因：双向协议能力更强，但状态管理、鉴权、重连与网关配置复杂度更高，不是 MVP 必需。

### 4. 状态机由系统控制，LLM 只产生建议和内容

任务状态包括 `PENDING`、`ANALYZING`、`PENDING_CLARIFICATION`、`PLANNING`、`PENDING_CONFIRMATION`、`CONFIRMED`、`EXECUTING`、`WAITING_USER_INPUT`、`COMPLETED`、`FAILED`、`CANCELLED`。状态转换由 `agent-core` 校验，Spring AI 模型输出不能直接修改关键状态。

备选：让 Agent 循环自由推进。放弃原因：无法审计、恢复和限制风险。

### 5. MySQL 存业务轨迹，PostgreSQL + pgvector 存知识向量

MySQL 适合平台业务表：会话、消息、任务、计划、事件、确认、审计、任务图节点与边。PostgreSQL + pgvector 适合向量检索，同时可用全文索引支撑混合检索。

pgvector 接入优先使用 Spring AI VectorStore；如当前版本能力不足，则在 `agent-rag` 内用 JDBC/MyBatis/JPA 实现 Java DAO，但不引入非 Java 向量服务作为 MVP 必需依赖。

备选：所有数据都放 PostgreSQL。放弃原因：当前 Docker Compose 已明确双库角色；保留双库能贴合现有规划。

### 6. DTO/API 合约优先，内部实现可逐步增强

前端只依赖 `taskStatus`、`eventType`、`actions`、`result`、`error` 等稳定字段，不依赖 Prompt、模型消息细节或工具内部类名。这样后续可替换模型渠道、RAG 检索策略或工具实现。

结构化协议使用 Java `record`/class DTO、Bean Validation、Jackson 和 OpenAPI 注解，而不是 Python/Pydantic 生态。

### 7. 前端任务以 API 合约并行推进

前端可在后端未完全实现时基于 OpenAPI/Mock 数据开发：任务创建表单、会话列表、SSE 时间线、计划确认卡片、工具确认卡片、记忆管理和 RAG 文档页面。

### 8. Java DAG 任务图编排

任务图只保留通用 DAG 思想：nodes、edges、blockedBy、ready、claim、complete、fail、unlock、visualize。不得依赖 Python TaskBoard/TaskManager 实现或文件 JSON runtime。

Java 平台采用 MySQL 持久化版本：

```text
agent_task_graph
  ├── agent_task_node       # node_key, subject, description, status, priority, owner, input_json, output_json
  ├── agent_task_edge       # from_node_key -> to_node_key
  └── agent_task_node_event # node_ready/node_started/node_completed/node_failed
```

核心算法：

1. 写入计划时保存 node 与 edge。
2. 计算 `blockedBy`：每个节点的入边来源。
3. `getReadyNodes(taskId)` 返回 `PENDING` 且所有依赖 `COMPLETED` 的节点。
4. `claimNode(taskId, nodeKey, workerId)` 使用事务与条件更新原子执行 `PENDING -> IN_PROGRESS`，避免并发重复领取。
5. `completeNode(...)` 写结果并将节点置为 `COMPLETED`，然后重新计算被解锁节点并发出事件。
6. `executeGraph(...)` 以循环方式取 ready nodes，使用 `ThreadPoolTaskExecutor` / `CompletableFuture` 并行执行同一层或同一批 ready nodes。
7. 图完成条件：所有节点为 `COMPLETED`；失败策略由 plan policy 控制（fail-fast / continue-on-error / manual）。

API 扩展：

- `POST /agent/tasks/{taskId}/graph`：写入任务图计划。
- `GET /agent/tasks/{taskId}/graph`：读取图结构、节点状态和边。
- `GET /agent/tasks/{taskId}/graph/ready-nodes`：读取可执行节点。
- `POST /agent/tasks/{taskId}/graph/execute`：启动并行图执行。
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/claim`：领取节点。
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/complete`：完成节点并解锁依赖。
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/fail`：标记节点失败。

### 9. Java 生态产品化能力对标

| 能力 | 当前状态 | Java/Spring 生态落地方案 |
|---|---|---|
| Prometheus + Grafana 监控 | MVP 增强 | Spring Boot Actuator + Micrometer Prometheus Registry + Grafana dashboards；提供 JVM、HTTP、DB、模型、RAG、工具、任务维度指标 |
| 前端观测可视化控制台 | MVP 增强 | 前端 `/observability`，展示服务健康、Agent trace、DAG/timeline、模型 token/cost、RAG quality、multi-agent/research、memory/rule hits、tool risk |
| 流量健康监控控制台 | MVP 增强 | Spring MVC REST API + 前端 `/traffic-health`，展示 QPS、错误率、P95/P99、首包延迟、首 SSE 延迟、模型渠道状态、Sentinel 规则和 incident list |
| 首包/首 token 延迟 | MVP 增强 | Spring AI streaming adapter 记录 request_start、first_event_at、first_token_at、completed_at |
| 模型渠道优先级/熔断切换 | 缺失 | Spring AI ChatModel wrapper + Sentinel/Spring Cloud Alibaba Sentinel + Spring Retry/Resilience4j 补充 |
| 消息持久化与断线重连 | 后置 | `java-research-session-resume`: research event journal + SSE cursor resume + session switch recovery + state snapshot，研究进度实时持久化并可恢复 |
| Agent 自我进化知识库 | 后置 | `java-agent-self-evolving-knowledge`: memory candidate extraction、confirmed profile、pitfall memory、behavior rule distillation、Spring AI Advisor injection、审计与敏感信息保护 |
| Swarm / Supervisor multi-agent | 后置 | `java-multiagent-orchestration`: Java role registry + Spring AI + existing DAG/state machine + SSE，分别支持 peer swarm 和 supervisor-controlled execution |
| Bootstrap Token 预算动态加载 | 后置 | `java-bootstrap-context-loading`: ContextBudgetAllocator + Spring AI Prompt/Advisor + context block scoring/packing + overflow protection，按 system/user/memory/rag/tool/rule/output budget 组装上下文 |
| 两级缓存 | 后置 | `java-bootstrap-context-loading`: Caffeine L1 + Spring Data Redis L2，按知识块类型 TTL；仅在明确热路径后实现 |
| RAG 查询优化器 | 后置 | `java-rag-query-optimization-evaluation`: Java pipeline：classification、coreference、step-back、decomposition、multi-query、hybrid retrieval、rerank、MMR、context pack；带 baseline comparison 和 regression gates |
| 深度研究模式 | 后置 | `java-research-workflow`: Java task state machine + DAG executor + Spring AI + `java-multiagent-orchestration`，支持 STANDARD/DEEP、ToT planning、专家协作、并行研究、反思验证、SSE 流式报告和 Java PDF export |
| MCP 协议适配 | 后置 | `java-mcp-integration-management`: 仅通过 Java/Spring AI MCP client/server 或 Java HTTP/SSE adapter 接入；工具仍走 Java governance |
| 首次对话引导 | 后置 | `java-onboarding-context-archive`: Spring MVC onboarding flow + confirmed memory write |
| 上下文归档恢复 | 后置 | `java-onboarding-context-archive`: full conversation archive before compression + MySQL event journal + summary snapshot + replay/backtracking API |
| Markdown 导出 | 后置 | Java async export job，先 Markdown/JSON；PDF 使用 PDFBox/OpenHTMLToPDF 作为后续独立 change |
| RAG 检索评估 | 后置 | `java-rag-query-optimization-evaluation`: Java-native evaluation：hit@k、recall@k、MRR、citation coverage、empty retrieval rate、latency、query expansion count、context token cost；RAGAS 不进 MVP runtime |

### 10. Sentinel 模型渠道治理设计

模型调用不直接绑定单一 DeepSeek endpoint，而是通过 Java `ModelChannelRouter` 包装 Spring AI `ChatModel` / `StreamingChatModel`：

```text
Agent Executor
   │
   ▼
ModelChannelRouter
   ├── SpringAiChatModel channel list: deepseek-primary > deepseek-backup > mock
   ├── Sentinel resource: model:{provider}:{model}:{channel}
   ├── Micrometer metrics: qps/error/rt/first_token_latency/token_cost_estimate
   ├── circuit breaker: Sentinel slow call ratio / error ratio / exception count
   └── fallback: next healthy Spring AI channel or graceful mock/degraded answer
```

Sentinel 接入：

- Docker 服务：`bladex/sentinel-dashboard:1.8.8`。
- Dashboard 端口建议按项目偏移规则使用 `8914:8858`。
- 应用引入 Sentinel core/adapter 或 Spring Cloud Alibaba Sentinel 依赖，模型渠道调用以 channel key 作为 Sentinel resource。
- 规则类型：
  - FlowRule：限制单渠道 QPS。
  - DegradeRule：基于慢调用比例、异常比例熔断。
  - ParamFlowRule：按 model/user/task mode 做热点限流。
  - SystemRule：保护整体系统负载。
- 规则持久化：MVP 可本地配置；后续接 Nacos/MySQL。避免引入与当前 Java 栈不一致的配置中心作为 MVP 必需项。

### 11. 首包延迟与流式监控

首包延迟定义：

- `request_start_at`：收到用户请求时间。
- `model_call_start_at`：发起 Spring AI 模型调用时间。
- `first_token_at`：收到模型第一个 token 时间。
- `first_sse_event_at`：向前端发出第一个 SSE 事件时间。
- `completed_at`：任务完成时间。

指标：

- `agent_first_token_latency_ms = first_token_at - model_call_start_at`
- `agent_first_event_latency_ms = first_sse_event_at - request_start_at`
- `agent_total_latency_ms = completed_at - request_start_at`
- 维度：provider、model、channel、task_mode、status、fallback_used。

这些指标进入：

- `/api/observability/metrics/summary`
- `/api/observability/tasks/{taskId}/timeline`
- `/api/observability/tasks/{taskId}/trace`
- `/api/observability/model-costs`
- `/api/observability/rag-quality`
- `/api/observability/tool-risk`
- `/api/observability/memory-rules`
- `/api/observability/research-multiagent`
- `/api/traffic-health/summary`
- Prometheus `/api/actuator/prometheus`
- Grafana dashboard
- Sentinel 熔断判断输入

### 12. 前端监控可视化设计

前端必须提供可直接使用的监控界面，而不仅是 Prometheus/Grafana 外部面板。建议路由：

```text
/observability                # 总览：服务健康、QPS、错误率、延迟、任务、成本、incident
/observability/tasks/:taskId  # Agent trace：状态机、DAG、timeline、model/tool/RAG/memory 事件
/traffic-health               # 流量健康：首 token、首 SSE、P95/P99、channel status、Sentinel rules
/model-channels               # 模型渠道：优先级、熔断、fallback、成本、错误率
/rag-quality                  # RAG 质量：evaluation、baseline comparison、regression gates、低质量 query
/tool-risk                    # 工具风险：确认、拒绝、超时、重试、审计
/memory-rules                 # 自我进化知识：profile、pitfall、behavior rule 命中和冲突
/context-budget               # Token 预算：model window、section budget、selected/dropped/compressed blocks、overflow protection
/research-monitor             # 深度研究/multi-agent：ToT branches、expert roles、tracks、reflection、report export
```

前端组件建议：

- Summary cards：health、QPS、error rate、active tasks、token cost、incidents。
- Time-series charts：latency、first-token、first-SSE、model errors、retrieval latency。
- Trace timeline：任务事件、模型调用、工具调用、RAG 检索、Advisor 注入、错误与重试。
- DAG graph：任务图节点、依赖、ready/blocked/in-progress/completed/failed。
- Channel table：provider/model/channel、priority、enabled、Sentinel state、fallback、cost。
- RAG evaluation table：baseline vs optimized、recall@k、MRR、citation coverage、gate status。
- Risk/audit table：危险工具确认、规则应用、memory candidate、用户确认与回滚。
- Context budget view：model window、output reserve、section budgets、selected/dropped/compressed blocks、cache hits、overflow prevention。

## API Design

Base path: `/api`

### Sessions

- `POST /sessions`
  - Request: `{ "title": "string", "metadata": {} }`
  - Response: `{ "id": "string", "title": "string", "createdAt": "datetime" }`
- `GET /sessions?page=&size=`
- `GET /sessions/{sessionId}`
- `GET /sessions/{sessionId}/messages`
- `POST /sessions/{sessionId}/messages`
  - Request: `{ "role": "USER", "content": "string", "metadata": {} }`

### Agent Tasks

- `POST /agent/tasks`
  - Request: `{ "sessionId": "string", "input": "string", "mode": "CHAT|RAG|TOOL|RESEARCH", "requiresPlanConfirmation": true, "budget": { "maxSteps": 10, "maxTokens": 8000 } }`
  - Response: `{ "taskId": "string", "status": "PENDING", "streamUrl": "/api/agent/tasks/{taskId}/events" }`
- `GET /agent/tasks/{taskId}`
- `GET /agent/tasks/{taskId}/events` using `text/event-stream`
- `POST /agent/tasks/{taskId}/confirm-plan`
  - Request: `{ "approved": true, "comment": "string", "updatedPlan": {} }`
- `POST /agent/tasks/{taskId}/cancel`
- `POST /agent/tasks/{taskId}/retry`
- `GET /agent/tasks/{taskId}/result`

### Agent Task Graph

- `POST /agent/tasks/{taskId}/graph`
  - Request: `{ "goal": "string", "nodes": [{ "nodeKey": "analyze", "subject": "分析需求", "description": "...", "priority": 3, "executorType": "MOCK|SPRING_AI|TOOL|JAVA_SUBTASK", "input": {} }], "edges": [{ "from": "analyze", "to": "backend" }], "policy": { "failureMode": "FAIL_FAST|CONTINUE|MANUAL" } }`
  - Response: `{ "taskId": "string", "nodeCount": 4, "edgeCount": 4, "parallelGroups": [["analyze"], ["backend", "frontend"], ["verify"]] }`
- `GET /agent/tasks/{taskId}/graph`
- `GET /agent/tasks/{taskId}/graph/ready-nodes`
- `POST /agent/tasks/{taskId}/graph/execute`
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/claim`
  - Request: `{ "workerId": "agent-worker-1" }`
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/complete`
  - Request: `{ "workerId": "agent-worker-1", "result": {}, "summary": "string" }`
- `POST /agent/tasks/{taskId}/nodes/{nodeKey}/fail`
  - Request: `{ "workerId": "agent-worker-1", "error": "string" }`

### Spring AI Model / Context

- `POST /model/chat`
  - Optional development endpoint for verifying Spring AI chat integration.
- `POST /bootstrap/context-preview`
  - Request: `{ "sessionId": "string", "query": "string", "budget": 16000 }`
- `GET /bootstrap/metrics`

### Tool Confirmation

- `GET /agent/tasks/{taskId}/tool-calls`
- `POST /agent/tool-calls/{toolCallId}/confirm`
  - Request: `{ "approved": true, "reason": "string" }`

### Memory

- `POST /memories`
- `GET /memories?type=&query=&page=&size=`
- `PATCH /memories/{memoryId}`
- `DELETE /memories/{memoryId}`

### RAG / Knowledge

- `POST /rag/documents`
  - Request: `{ "title": "string", "content": "string", "sourceUri": "string", "metadata": {} }`
- `GET /rag/documents/{documentId}`
- `POST /rag/search`
  - Request: `{ "query": "string", "topK": 5, "mode": "VECTOR|KEYWORD|HYBRID" }`
- `POST /rag/evaluations`
  - Request: Java-native retrieval evaluation job using stored query/relevance datasets.

### Traffic Health / Model Governance

- `GET /traffic-health/summary`
  - Response: `{ "qps": 1.2, "errorRate": 0.01, "p95LatencyMs": 1200, "firstTokenP95Ms": 600, "channels": [] }`
- `GET /traffic-health/model-channels`
- `PUT /traffic-health/model-channels/{channelId}/priority`
- `POST /traffic-health/model-channels/{channelId}/disable`
- `POST /traffic-health/model-channels/{channelId}/enable`
- `GET /traffic-health/sentinel/rules`
- `PUT /traffic-health/sentinel/rules`
- `GET /traffic-health/first-token-latency?provider=&model=&window=`

### Java Research Workflow

- `POST /research/tasks`
- `GET /research/tasks/{taskId}`
- `GET /research/tasks/{taskId}/events`
- `POST /research/tasks/{taskId}/confirm-plan`
- `POST /research/tasks/{taskId}/intervention`
- `GET /research/tasks/{taskId}/report/export`

### Java MCP Management

MCP is not part of the MVP runtime unless a Java/Spring AI MCP integration is available. If implemented, it must use Java HTTP/SSE client/server code and route all tools through existing tool governance.

- `GET /mcp/servers`
- `POST /mcp/servers`
- `POST /mcp/servers/{serverId}/start`
- `POST /mcp/servers/{serverId}/stop`
- `GET /mcp/servers/{serverId}/tools`
- `POST /mcp/servers/{serverId}/tools/{toolName}/execute`

### Observability

- `GET /observability/tasks/{taskId}/timeline`
- `GET /observability/tasks/{taskId}/trace`
- `GET /observability/metrics/summary`
- `GET /observability/model-costs?provider=&model=&channel=&taskMode=&window=`
- `GET /observability/rag-quality?collection=&strategy=&window=`
- `GET /observability/tool-risk?toolName=&permissionLevel=&window=`
- `GET /observability/memory-rules?taskId=&ruleType=&window=`
- `GET /observability/research-multiagent?taskId=&mode=&window=`
- `GET /observability/incidents?severity=&status=&window=`
- `GET /actuator/health`
- `GET /actuator/prometheus`

## Data Model

Minimum MySQL tables:

- `agent_session`: id, title, metadata, created_at, updated_at, is_delete
- `conversation_message`: id, session_id/conversation_id, role, content, metadata, created_at, updated_at, is_delete
- `agent_task`: id, session_id, input, mode, status, risk_level, budget_json, result_json, error_code, error_message, created_at, updated_at
- `agent_task_node`: id, task_id, node_key, subject, description, status, priority, executor_type, owner, input_json, output_json, error_message, created_at, updated_at
- `agent_task_edge`: id, task_id, from_node_key, to_node_key, edge_type, created_at
- `agent_task_node_event`: id, task_id, node_key, event_type, payload_json, created_at
- `agent_task_plan`: id, task_id, version, status, plan_json, created_at
- `agent_task_event`: id, task_id, event_type, sequence_no, payload_json, created_at
- `agent_tool_call`: id, task_id, tool_name, permission_level, input_json, output_json, status, requires_confirmation, confirmed_by, confirmed_at, created_at
- `agent_audit_log`: id, actor_id, action, target_type, target_id, payload_json, created_at
- `agent_memory`: id, memory_type, content, source, confidence, metadata, created_at, updated_at, is_delete
- `model_channel`: id, provider, model, channel_key, priority, enabled, config_json, created_at, updated_at
- `rag_evaluation_case`: id, query, expected_document_ids_json, expected_chunk_ids_json, metadata, created_at
- `rag_evaluation_run`: id, status, metrics_json, created_at, completed_at

Minimum PostgreSQL tables:

- `document_embeddings`: id, document_id, chunk_id, content, embedding vector(1024), metadata jsonb, created_at, updated_at
- `document_chunk`: optional if document metadata is separated from embedding rows

## Risks / Trade-offs

- [Risk] 仓库当前工程结构范围较大 → Mitigation: 先保证 Java/Spring Boot 可编译骨架、健康检查和 Mock Agent，再迭代 Spring AI 真实模型/RAG。
- [Risk] 双数据库提高配置和测试复杂度 → Mitigation: 明确 MySQL/PGVector 职责，提供 Docker profile 和集成测试。
- [Risk] SSE 连接在代理或浏览器刷新时中断 → Mitigation: 事件持久化并提供 timeline 回放接口。
- [Risk] 真实 LLM 调用可能受密钥或网络影响 → Mitigation: `agent-model` 提供 Spring AI mock profile，并通过 profile 切换。
- [Risk] Spring AI 版本能力与某些高级需求不完全匹配 → Mitigation: 优先用 Spring AI 抽象；不足处在 Java 模块内补 adapter，不引入非 Java 主链路。
- [Risk] PostgreSQL 初始化 SQL 当前包含 MySQL 风格字段 COMMENT → Mitigation: 在实现时修正为 PostgreSQL 合法 `COMMENT ON` 语句并用容器启动验证。
- [Risk] 前端与后端并行时字段理解不一致 → Mitigation: 以 OpenAPI/DTO 和本文 API Design 为契约。
