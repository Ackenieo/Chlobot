## Why

当前仓库已经具备 Docker、数据库初始化和开发规范，但项目计划中混入了部分非 Java 生态、Python 优先或跨生态 Agent 工具表述。后续实现必须收敛为 Java/Spring Boot/Spring AI 主线：能用 Spring AI、Spring Boot、Spring Data、Spring Cloud Alibaba、Micrometer、JDK 并发和 Java 生态产品完成的能力，才进入主计划；非 Java 工具只能作为可选离线辅助，不进入 MVP 主链路。

本变更将 Agent MVP 计划重写为 Java 生态优先版本，明确 Spring AI 是模型、Prompt、Embedding、VectorStore、Tool Calling、RAG 与结构化输出的首选抽象。

## What Changes

- 建立 Spring Boot 3 + JDK 21 的 Maven 多模块工程骨架，补齐现有 Dockerfile 预期的模块结构。
- 引入 `common`、`agent-core`、`agent-tools`、`agent-memory`、`agent-rag`、`agent-model`、`agent-sandbox`、`app` 模块分层。
- 将 Spring AI 作为 AI 能力主线：ChatModel、StreamingChatModel、EmbeddingModel、VectorStore、Tool Calling、Advisor/RAG、Structured Output。
- 将 DeepSeek 按 OpenAI-compatible provider 接入 Spring AI OpenAI 兼容配置；避免直接在业务 Controller 调供应商 SDK。
- 将 MySQL 用于会话、消息、任务、事件、计划、工具调用、审计日志等核心业务持久化。
- 将 PostgreSQL + pgvector 通过 Spring AI VectorStore / JDBC DAO 用于文档向量、混合检索与知识检索支撑。
- 提供面向前端的 REST + SSE 接口：会话、消息、任务创建、计划确认、状态查询、事件流、取消、重试、工具确认、记忆 CRUD、文档导入、检索、健康检查与观测接口。
- 用 Java 原生/Spring 方式实现 DAG 任务图编排：MySQL 持久化节点与边、事务性 claim、依赖解锁、`ThreadPoolTaskExecutor` / `CompletableFuture` 并行执行。
- 使用 Java 生态观测与治理：Spring Boot Actuator、Micrometer、Prometheus Registry、Grafana Dashboard、Sentinel/Spring Cloud Alibaba Sentinel、Spring Retry/Resilience4j（按场景选）。
- 将非 Java 优先概念替换为 Java 对标方案：RAGAS 改为 Java-native RAG evaluation；LangChain/LangGraph/LlamaIndex 类能力改为 Spring AI + 自研编排；Python TaskBoard 仅保留 DAG 思想，不作为实现依赖。
- 对 Bootstrap Context、Deep Research、Research Session Resume、MCP、RAG Query Optimizer、Onboarding/Context Archive、Multi-Agent Swarm/Supervisor、Agent Self-Evolving Knowledge 等高级亮点能力，不删除功能，而是拆成 Java/Spring follow-up changes：`java-bootstrap-context-loading`、`java-research-workflow`、`java-research-session-resume`、`java-mcp-integration-management`、`java-rag-query-optimization-evaluation`、`java-onboarding-context-archive`、`java-multiagent-orchestration`、`java-agent-self-evolving-knowledge`。
- **BREAKING**: 当前项目计划不再接受 Python-first 或跨语言服务作为 MVP 主链路依赖，后续实现以模块化 Java/Spring 服务为主线。


## Java Ecosystem Policy

- MUST 优先使用 Spring Boot、Spring AI、Spring Data、Spring Security、Spring Integration、Spring Cloud Alibaba、Micrometer、JDK 21 并发能力。
- MUST 在出现库生态选择时寻找 Java/Spring 对标产品。
- MUST 将非 Java 工具降级为可选离线辅助，除非它提供稳定 Java SDK 或 Spring Boot starter。
- SHOULD 使用 Spring AI 抽象模型、Embedding、VectorStore、Tool Calling、RAG 和 Structured Output。
- SHOULD 使用 Java `record`、Bean Validation、Jackson、OpenAPI DTO 作为结构化协议，而不是 Python/Pydantic 类生态。

## Capabilities

### New Capabilities

- `agent-task-platform`: Java/Spring Boot Agent 任务编排、状态机、事件流、确认与恢复能力。
- `agent-task-graph-orchestration`: Java DAG 任务图、依赖解锁、就绪节点查询、事务性节点领取和并行执行能力。
- `session-message-management`: 会话、消息与历史轨迹管理能力。
- `tool-confirmation-governance`: 基于 Spring AI Tool Calling 对齐的工具注册、危险工具确认、审计与幂等治理能力。
- `memory-management`: MySQL 持久化记忆写入、查询、编辑与删除能力，可与 Spring AI Chat Memory/Advisor 集成。
- `rag-knowledge-retrieval`: 基于 Spring AI EmbeddingModel、VectorStore、PostgreSQL pgvector 的文档导入、向量化、混合检索与引用能力。
- `platform-observability`: Spring Boot Actuator、Micrometer、Prometheus、Grafana、日志、事件回放与成本观测能力。
- `traffic-health-console`: 流量健康监控控制台、Prometheus/Grafana/Sentinel Dashboard 接入、首包延迟与模型渠道健康可视化能力。
- `model-channel-governance`: 基于 Spring AI ChatModel 包装器 + Sentinel/Resilience4j/Spring Retry 的模型渠道优先级、熔断、降级、限流和重试治理能力。

### Java Follow-up Changes

- `java-bootstrap-context-loading`: 基于 Spring AI Prompt/Advisor 的 Token 预算动态上下文组装与 Java 缓存能力。
- `java-rag-query-optimization-evaluation`: 基于 Spring AI 和 Java 服务实现指代消解、Step-Back、问题分解、多查询改写、Rerank/MMR，以及 Java-native retrieval evaluation。
- `java-research-workflow`: 基于 Java 任务状态机和 DAG Executor 的研究任务，支持计划确认、并行 track、反思验证、SSE 报告流。
- `java-mcp-integration-management`: 仅在 Java/Spring AI MCP client/server 支持可用时接入 MCP server 注册、工具发现和 Streamable HTTP/SSE 传输；所有工具仍进入现有 Java 工具治理层。
- `java-onboarding-context-archive`: Java/Spring onboarding、确认用户画像、上下文归档与恢复能力。

### Removed / Reframed Capabilities

- 移除 Python-first RAGAS 主链路；改为 Java-native RAG evaluation，RAGAS 仅可作为离线可选工具。
- 移除对 LangChain/LangGraph/LlamaIndex 等非 Java Agent 框架的主链路依赖；改用 Spring AI + Java 编排。
- 移除对 Python TaskBoard/TaskManager 实现的依赖；只保留 DAG/blockedBy/ready/claim/complete 的通用设计思想。
- PDF 导出、复杂 Onboarding、完整上下文归档恢复后置；MVP 仅保留 Markdown/JSON 导出和事件回放基础。

## Impact

- 代码结构：新增/修正多模块 Maven 工程、Spring Boot 启动模块与 Java 公共基础设施模块。
- API：新增任务、会话、SSE 事件、工具确认、记忆、RAG、任务图与健康检查接口。
- 数据库：补充 MySQL 业务表设计与 PostgreSQL pgvector 向量检索表设计。
- 依赖：优先引入 Spring Boot Web、Validation、Actuator、Spring AI、Spring Data、MySQL/PostgreSQL Driver、Micrometer、Sentinel/Spring Cloud Alibaba Sentinel、测试依赖。
- 系统：需要与 Docker Compose、启动脚本、健康检查、Swagger UI、Prometheus/Grafana/Sentinel Dashboard 和后续前端联动。
