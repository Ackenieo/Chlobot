## Why

Chlobot already persists task events and supports SSE replay at the Agent task level, but the product highlight also needs research progress to survive client disconnects and session switches. Research state should be recoverable from persisted events, cursors, and snapshots instead of being tied only to an open browser connection.

本 change 将研究会话恢复能力作为 Java follow-up：通过 MySQL 事件日志、SSE 游标、研究任务状态和摘要快照恢复研究进度，不引入非 Java 流式会话 runtime。

## What Changes

- 新增 research session persistence and resume 规划。
- 支持研究任务与会话绑定，客户端可在会话切换后重新连接到同一研究任务。
- 支持 SSE reconnect / event cursor resume，断线后可从已确认的事件位置继续推送增量事件。
- 支持 research state snapshot，保存当前阶段、ToT 分支、并行 track、反思点、等待用户确认状态和导出状态。
- 支持 session switch recovery，依据 session/task/replay metadata 恢复当前研究视图。
- 支持恢复后的事件补发、进度补偿和重复事件去重。
- 支持把恢复信息暴露给前端研究监控页面和任务页面。

## Non-Goals

- 不实现 WebSocket 双向流作为主方案。
- 不把前端 tab 恢复当作唯一恢复机制。
- 不引入非 Java 会话流框架。
- 不替代完整上下文归档能力；该能力仍由 `java-onboarding-context-archive` 负责。

## Capabilities

- `java-research-session-resume`: Java research session persistence, SSE reconnect, cursor resume, session switch recovery, and state snapshot restore.

## Impact

- `agent-core`: 扩展研究任务状态与恢复元数据。
- `agent-memory` / `agent-core`: 保存事件游标、快照、恢复点和 replay metadata。
- `app`: 增加研究会话恢复 API、恢复状态查询和前端重连支持。
- 前端：研究监控页和任务页需要显示恢复点、断线状态和当前游标。
