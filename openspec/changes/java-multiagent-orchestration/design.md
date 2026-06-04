## Context

现有 MVP 以 Java 单编排器、状态机和 DAG task graph 为主线；`java-research-workflow` 会进一步提供研究任务、并行 track、反思验证与报告流。Multi-agent 编排应建立在这些能力之上，作为产品亮点后置增强。

## Goals

- 提供两种明确 multi-agent 模式：Swarm 和 Supervisor。
- 保持 Java/Spring AI 生态实现：Spring AI ChatModel/StreamingChatModel、Prompt/Advisor、Tool Calling、RAG、Structured Output。
- 复用现有 DAG executor、SSE、tool governance、memory、RAG、model channel governance。
- 让 agent role、任务分派、投票、评审、artifact 和最终合成都可审计、可恢复。

## Non-Goals

- 不实现 AutoGen/CrewAI/LangGraph 风格的跨语言 runtime。
- 不让 LLM 自由创建不可控 agent；角色由 Java registry 和 policy 限制。
- 不绕过危险工具确认和审计。

## Decisions

### 1. Multi-agent mode 是任务策略，不是独立 runtime

`SWARM` 和 `SUPERVISOR` 应作为 Java task policy / graph execution policy：

```text
AgentTask
  └── MultiAgentPolicy(mode=SWARM|SUPERVISOR)
        ├── roleSet
        ├── coordinationStrategy
        ├── verificationPolicy
        ├── budget
        └── outputContract
```

这样可以复用已有任务状态、事件、DAG node、模型治理和工具治理。

### 2. Agent Role Registry 使用 Java/Spring AI 配置角色

角色不是独立进程，而是 Java service 配置：

```text
AgentRole
  ├── roleKey: PLANNER | RESEARCHER | CODER | REVIEWER | VERIFIER | SYNTHESIZER | TOOL_OPERATOR
  ├── promptTemplate / Spring AI Advisor chain
  ├── allowedTools
  ├── ragPolicy
  ├── memoryPolicy
  ├── modelChannelPolicy
  └── outputSchema
```

### 3. Swarm 模式

Swarm 适合开放式、多角度、需要互相验证的任务。多个 peer agents 对同一个目标或子目标并行探索，然后通过 critic/vote/synthesis 合成。

典型流程：

```text
goal
  ├── peer agents parallel explore
  ├── cross-review / adversarial verify
  ├── vote or score
  └── synthesizer produces final result
```

适用场景：

- 多方案设计评审
- 安全/质量/性能多视角 review
- 研究任务多来源探索
- 复杂问题的独立思路对比

### 4. Supervisor 模式

Supervisor 适合工程化、长流程和高风险任务。Supervisor 负责规划、分派、质量门禁、重试和最终合成；worker agents 只执行被分派的子任务。

典型流程：

```text
supervisor
  ├── decompose plan into DAG
  ├── assign worker roles
  ├── monitor progress/events
  ├── request review or retry when quality gate fails
  └── synthesize final answer
```

适用场景：

- 代码实现/重构/迁移
- 长链路工程任务
- 涉及工具确认或外部系统的任务
- 需要明确 owner 与验收门禁的任务

### 5. 数据模型草案

最小 MySQL 表可按现有 task tables 扩展：

```text
agent_role_definition
agent_multiagent_session
agent_multiagent_assignment
agent_multiagent_message
agent_multiagent_artifact
agent_multiagent_vote
agent_multiagent_decision
agent_multiagent_review
```

其中 `agent_multiagent_assignment` 关联 task graph node，记录 role、mode、status、owner、input/output JSON、budget 和 trace metadata。

### 6. API 草案

Base path: `/api`

```text
POST /multi-agent/tasks
GET /multi-agent/tasks/{taskId}
GET /multi-agent/tasks/{taskId}/events
POST /multi-agent/tasks/{taskId}/confirm-plan
POST /multi-agent/tasks/{taskId}/intervention
GET /multi-agent/tasks/{taskId}/artifacts
GET /multi-agent/roles
POST /multi-agent/roles
PATCH /multi-agent/roles/{roleKey}
```

`POST /multi-agent/tasks` request 示例：

```json
{
  "sessionId": "string",
  "goal": "string",
  "mode": "SWARM",
  "roles": ["RESEARCHER", "REVIEWER", "SYNTHESIZER"],
  "requiresPlanConfirmation": true,
  "budget": { "maxAgents": 5, "maxSteps": 20, "maxTokens": 32000 }
}
```

### 7. 事件类型草案

```text
MULTI_AGENT_SESSION_CREATED
AGENT_ROLE_ASSIGNED
AGENT_STARTED
AGENT_MESSAGE_DELTA
AGENT_ARTIFACT_CREATED
AGENT_REVIEW_REQUESTED
AGENT_REVIEW_COMPLETED
AGENT_VOTE_CAST
SUPERVISOR_DECISION_MADE
SWARM_SYNTHESIS_STARTED
MULTI_AGENT_COMPLETED
MULTI_AGENT_FAILED
```

## Open Questions

- 首版是否只内置固定角色，还是允许管理员配置 role prompt？建议首版固定核心角色，配置能力后置。
- Swarm 投票是否要求结构化 score schema？建议必须结构化，便于审计和合成。
- Supervisor 是否允许动态新增 DAG node？建议允许，但必须通过 Java state transition 和事件审计。
