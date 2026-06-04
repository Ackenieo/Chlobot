## Context

本 change 负责把“深度研究”从泛化 Agent 框架概念落到 Java/Spring Boot 实现。研究任务不是聊天窗口，而是可追踪、可确认、可并行、可回放的 Java 编排任务。

## Goals / Non-Goals

**Goals:**
- 支持 `STANDARD` / `DEEP` 两种研究模式。
- 支持研究任务澄清与计划确认。
- 支持 ToT(Tree-of-Thought) 多路径规划、候选计划评分与确认。
- 支持基于 `java-multiagent-orchestration` 的专家型 Agent 协作。
- 支持并行 research tracks。
- 支持反思验证与证据一致性检查。
- 支持 SSE 事件流、流式报告和 Markdown/JSON/PDF 导出。

**Non-Goals:**
- 不依赖非 Java Agent runtime。
- 不把 PDF 导出作为 MVP 必需项。
- 不引入 Python-first workflow library。

## Decisions

### 1. Java task graph as the engine

研究流程由 Java DAG executor 驱动，而不是外部 workflow runtime。

### 2. Spring AI as reasoning layer

研究摘要、提纲、反思、报告生成都通过 Spring AI 抽象完成。

### 3. SSE for progress

研究过程中必须能流式输出进度和中间结果。

## API Design

- `POST /research/tasks`
- `GET /research/tasks/{taskId}`
- `GET /research/tasks/{taskId}/events`
- `POST /research/tasks/{taskId}/confirm-plan`
- `POST /research/tasks/{taskId}/intervention`
- `GET /research/tasks/{taskId}/report/export`

## Data Model

- `research_task`
- `research_track`
- `research_event`
- `research_report`
