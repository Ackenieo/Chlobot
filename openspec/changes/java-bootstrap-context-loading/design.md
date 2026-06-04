## Context

本 change 专注于 Java/Spring AI 的上下文装配能力，不依赖 Python-first Agent 框架。目标是在明确 token 预算内，把 system、user、memory、RAG、tool result 和 output reserve 组合成可用于 Spring AI 调用的上下文。

## Goals / Non-Goals

**Goals:**
- 构建 Spring AI Prompt / Advisor 上下文装配链路。
- 以 Java `ContextBudgetAllocator` 实现 model window、output reserve、section budgets 和 safety margin 控制。
- 支持确认记忆、RAG 结果、最近对话、工具结果、行为规则和研究/任务状态的优先级装配。
- 支持 context block scoring、去重、压缩/摘要、context packing 和 citation/source preservation。
- 支持 hard max token overflow protection、section-level truncation 和 fallback summarization。
- 提供缓存、preview、metrics 和前端预算可视化能力。

**Non-Goals:**
- 不把 Redis 作为 MVP 必需项。
- 不把复杂 onboarding 作为本 change 的强制内容。
- 不引入非 Java context-framework 作为实现依赖。

## Decisions

### 1. Spring AI Prompt / Advisor 优先

上下文装配直接服务于 Spring AI 调用，而不是外部工作流框架。

### 2. 缓存分层后置优化

默认从 Java 本地缓存开始；只有在明确热路径或重复上下文明显时，才引入 Redis L2。

### 3. 预算是硬约束

上下文块按 token 预算纳入或排除，避免模型上下文不可控膨胀。

### 4. ContextBudgetAllocator

预算分配器必须明确模型窗口、输出预留和各 section 预算：

```text
ContextBudgetAllocator
  ├── modelWindowTokens
  ├── outputReserveTokens
  ├── safetyMarginTokens
  ├── systemBudget
  ├── userInputBudget
  ├── recentMessageBudget
  ├── memoryBudget
  ├── ragBudget
  ├── toolResultBudget
  └── behaviorRuleBudget
```

预算分配应支持按 task mode、model window、research depth、RAG usage、tool result size 和 user preference 动态调整。

### 5. Context block scoring and packing

候选上下文块进入装配前需要评分：

```text
score = relevance * w1
      + freshness * w2
      + authority * w3
      + userPreference * w4
      + taskNeed * w5
      + recency * w6
      - tokenCostPenalty
```

装配流程：

```text
Candidate Blocks
  -> estimate tokens
  -> score
  -> deduplicate
  -> preserve citations/source
  -> compress/summarize if needed
  -> pack by section budget
  -> reserve output tokens
  -> reject overflow
```

### 6. Overflow protection

必须保护模型窗口：

- hard max token limit 不可突破。
- output reserve 不可被上下文占用。
- section-level truncation 必须记录 dropped reason。
- fallback summarization 只能在保留 source/citation 的前提下压缩。
- preview 必须展示 selected、dropped、compressed 和 overflow-prevented blocks。

## API Design

- `POST /bootstrap/context-preview`
- `GET /bootstrap/metrics`

## Data Model

- `bootstrap_context_preview_log`
- `bootstrap_context_budget`
