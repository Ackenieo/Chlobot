## Why

Chlobot 需要在 DAG 任务图与 Java research workflow 之后继续承接多 Agent 协作亮点能力。该能力必须保留 Swarm 与 Supervisor 两种 multi-agent 模式，但实现必须收敛在 Java/Spring Boot/Spring AI 生态内，不引入 AutoGen、CrewAI、LangGraph 等非 Java 主链路框架。

本 change 将 multi-agent 能力定义为后置 Java/Spring follow-up：复用现有任务状态机、DAG executor、SSE 事件、工具治理、记忆与 RAG，而不是另起跨语言 runtime。

## What Changes

- 新增 Java multi-agent 编排规划，包含两种模式：`SWARM` 与 `SUPERVISOR`。
- 定义 Java Agent Role Registry，用 Spring AI Prompt/Advisor、ChatModel/StreamingChatModel、Tool Calling、RAG 和记忆上下文配置角色能力。
- 定义 Swarm 模式：多个同级角色围绕共享任务并行探索、互评、投票、合成，适合开放式研究、方案评审、代码审查和多角度验证。
- 定义 Supervisor 模式：一个 supervisor/planner 负责任务拆解、角色分派、质量门禁和最终合成，worker agents 执行受控子任务，适合工程执行、长流程任务和高风险工具调用。
- 复用 Java DAG task graph 作为执行底座：每个节点可绑定 `agentRole`、`multiAgentMode`、输入上下文、输出契约和 verification policy。
- 复用 MySQL 事件与 artifact 持久化，记录 agent assignment、message、vote、decision、artifact、review 和 final synthesis。
- 通过 SSE 输出 multi-agent timeline，前端可以展示各角色状态、分歧、投票、supervisor 决策和最终结果。

## Non-Goals

- 不把 Swarm/Supervisor 放入当前 MVP 必做能力。
- 不引入 Python-first 或非 Java Agent 框架作为 runtime。
- 不允许 multi-agent 绕过现有工具确认、权限、审计、模型渠道治理和 RAG 数据权限。
- 不在本 change 中实现完整 UI，只定义 API/事件/数据模型规划。

## Impact

- `agent-core`: 增加 multi-agent coordinator、mode policy、role assignment、decision/vote 合成模型。
- `agent-model`: 为不同 agent role 提供 Spring AI Prompt/Advisor 和模型调用配置。
- `agent-tools`: 所有角色工具调用继续进入统一 Java tool governance。
- `agent-memory` / `agent-rag`: 为角色提供受预算约束的共享上下文与检索能力。
- `app`: 后续新增 multi-agent task API、SSE timeline 和 OpenAPI DTO。
