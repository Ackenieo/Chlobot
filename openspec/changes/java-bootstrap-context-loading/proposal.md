## Why

当前 MVP 计划已经把 Spring AI、任务编排、会话、工具、记忆、RAG、观测等主线收敛到了 Java 生态，但上下文组装仍需要一个独立的 Java follow-up change 来完成：使用 Spring AI Prompt / Advisor 构建分层上下文、按 token 预算装配上下文块，并在需要时引入 Caffeine 与 Spring Data Redis 作为缓存实现。

## What Changes

- 使用 Spring AI Prompt / Advisor 设计上下文装配服务。
- 采用 Java `ContextBudgetAllocator` 分配 model window、output reserve、system、user、recent messages、memory、RAG、tool result 和 safety margin 预算。
- 定义确认记忆、检索片段、工具结果和最近对话的上下文装配优先级。
- 对候选知识块进行 relevance、freshness、authority、userPreference、taskNeed、recency 和 tokenCost 评分。
- 支持去重、压缩/摘要、fallback summarization、section-level truncation 和 hard max token overflow protection。
- 引入 Caffeine L1 缓存；若有明确热路径，再评估 Spring Data Redis L2。
- 提供上下文 preview API、预算可视化 API 和指标，便于验证预算、丢弃块、压缩块与命中情况。

## Capabilities

- `java-bootstrap-context-loading`: Spring AI 上下文装配、预算控制、缓存和预览能力。

## Impact

- 代码结构：新增上下文装配服务、预算模型、缓存适配器。
- API：新增上下文预览和指标接口。
- 依赖：Spring AI、Caffeine、可选 Spring Data Redis、Micrometer。
