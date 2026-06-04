## Context

MVP 已规划 `memory-management`，负责基础记忆 CRUD；`java-onboarding-context-archive` 负责 onboarding、confirmed profile memory 和上下文归档恢复。本 change 负责更高层的“自我进化”：从对话和任务经验中提炼可复用知识，并通过 Java/Spring AI 机制影响后续 Agent 行为。

## Goals / Non-Goals

**Goals:**
- 支持对话中自主发现候选记忆。
- 支持用户画像、偏好、项目规则、踩坑经验和行为规则。
- 支持置信度、确认、去重、合并、过期、禁用和审计。
- 支持从 confirmed memories 和 repeated corrections 中提炼 behavior rules。
- 支持通过 Spring AI Advisor 将画像与行为规则注入后续任务。
- 支持用户查看、确认、编辑、删除和回滚记忆/规则。

**Non-Goals:**
- 不让 Agent 自动写入不可撤销长期记忆。
- 不存储密钥、token、密码或敏感凭证。
- 不使用非 Java 记忆服务作为主链路 runtime。
- 不替代项目代码、Git 历史或 OpenSpec 文档作为事实来源。

## Decisions

### 1. 分层记忆生命周期

记忆状态必须分层，避免模型推断污染长期画像：

```text
OBSERVED -> CANDIDATE -> INFERRED -> CONFIRMED -> RULE -> DEPRECATED
```

- `OBSERVED`: 从对话/事件中观察到的原始线索。
- `CANDIDATE`: Java extractor 生成的候选记忆。
- `INFERRED`: 模型推断但尚未确认的事实或偏好。
- `CONFIRMED`: 用户确认或规则允许持久化的事实。
- `RULE`: 从多条 confirmed memory / correction / pitfall 中提炼出的行为规则。
- `DEPRECATED`: 被用户否定、过期或冲突替代。

### 2. 记忆类型

```text
USER_PROFILE
USER_PREFERENCE
PROJECT_PREFERENCE
WORKFLOW_RULE
PITFALL
CORRECTION
CONFIRMED_FACT
BEHAVIOR_RULE
TOOL_USAGE_RULE
DOMAIN_KNOWLEDGE
```

### 3. 自主提炼 Pipeline

```text
Conversation / Task Event / Tool Result / User Correction
  -> Candidate Extraction
  -> Classification
  -> Confidence Scoring
  -> Deduplication / Merge
  -> Sensitivity Check
  -> Confirmation Policy
  -> Persist Memory
  -> Rule Distillation
  -> Advisor Injection
  -> Application Audit
```

### 4. 用户画像模型

用户画像可包含：

```text
UserProfile
  ├── role
  ├── expertise
  ├── languagePreference
  ├── techStackPreference
  ├── communicationStyle
  ├── decisionPreference
  ├── riskTolerance
  ├── frequentlyUsedTools
  ├── confirmedConstraints
  └── updatedAt / source / confidence
```

所有 profile facts 必须可溯源、可编辑、可删除。

### 5. 踩坑经验模型

```text
PitfallMemory
  ├── trigger
  ├── mistake
  ├── correction
  ├── why
  ├── howToApply
  ├── relatedProject
  ├── confidence
  └── sourceEvents
```

### 6. 行为规则模型

行为规则使用结构化形式，便于 Java 代码做匹配和注入：

```text
BehaviorRule
  ├── condition
  ├── action
  ├── priority
  ├── scope: USER | PROJECT | GLOBAL
  ├── sourceMemoryIds
  ├── enabled
  └── version
```

示例：

```text
IF planning capability uses non-Java ecosystem
THEN translate it to Java/Spring/Spring AI counterpart
AND preserve product highlight as follow-up change
```

### 7. Spring AI Advisor 注入

后续任务上下文可按预算注入：

```text
ProfileAdvisor
PreferenceAdvisor
PitfallAdvisor
BehaviorRuleAdvisor
```

Advisor 必须输出 trace，记录注入了哪些 memory/rule，以及为什么注入。

### 8. API Design

Base path: `/api`

```text
GET /knowledge/memory-candidates
POST /knowledge/memory-candidates/{candidateId}/confirm
POST /knowledge/memory-candidates/{candidateId}/reject
GET /knowledge/profile
PATCH /knowledge/profile/{factId}
DELETE /knowledge/profile/{factId}
GET /knowledge/pitfalls
POST /knowledge/pitfalls
PATCH /knowledge/pitfalls/{pitfallId}
GET /knowledge/behavior-rules
POST /knowledge/behavior-rules/{ruleId}/enable
POST /knowledge/behavior-rules/{ruleId}/disable
POST /knowledge/behavior-rules/{ruleId}/rollback
GET /knowledge/application-audit?taskId=
```

### 9. Data Model

```text
agent_memory_candidate
agent_profile_fact
agent_pitfall_memory
agent_behavior_rule
agent_behavior_rule_version
agent_memory_source_event
agent_memory_application_audit
```

## Open Questions

- 哪些记忆类型可以自动确认？建议首版只有低风险 project/workflow preference 可按策略自动确认，用户画像必须用户确认。
- 行为规则冲突如何处理？建议按 scope、priority、updatedAt 和 user override 决定。
- 是否需要定期压缩记忆？建议作为后续 maintenance job。
