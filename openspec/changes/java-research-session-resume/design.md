## Context

研究工作流已经有 Java task state machine、DAG executor、Spring AI、SSE 和 `java-multiagent-orchestration`。本 change 专注于“研究会话可恢复”这一层：客户端断线、切换会话或页面刷新后，应能基于事件游标和状态快照回到同一研究进度。

## Goals / Non-Goals

**Goals:**
- Persist research progress and replay metadata.
- Resume SSE delivery from a known cursor.
- Restore active research state after session switch or client reconnect.
- Keep ordering and deduplication for resumed events.
- Surface recovery status to frontend monitoring pages.

**Non-Goals:**
- No WebSocket-based streaming requirement.
- No non-Java realtime session framework.
- No replacement of full context archive.

## Decisions

### 1. Event log is the source of truth

研究状态通过事件日志 + 快照恢复，而不是前端内存态。

### 2. Cursor-based resume

SSE 连接必须带 cursor/replay token，恢复时从已确认位置继续补发增量事件。

### 3. Snapshot + delta replay

恢复时返回：

- latest snapshot
- cursor
- pending delta events
- active stage / track / branch summary

### 4. Session switch recovery

同一研究任务可以从不同会话入口恢复，只要用户权限和 task binding 满足条件。

## API Design

- `POST /research/tasks/{taskId}/resume`
- `GET /research/tasks/{taskId}/resume-state`
- `GET /research/tasks/{taskId}/events?cursor=`
- `POST /research/tasks/{taskId}/cursor/ack`
- `GET /research/sessions/{sessionId}/active-tasks`
- `POST /research/sessions/{sessionId}/attach-task`

## Data Model

- `research_session`
- `research_task_cursor`
- `research_task_snapshot`
- `research_task_resume_event`

## Open Questions

- Cursor should be per session or per task?建议首版以 task 为主，session 仅作为 attach 入口。
- 是否允许多端同时订阅同一 task?建议允许，但事件补发必须幂等。
