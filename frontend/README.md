# Chlobot React Frontend

Chlobot 前端已切换为 `frontend/` 下的 **React + TypeScript + Vite** 单页控制台。后端仍按 `/api` 前缀提供接口，完整前端接口契约维护在 `../openspec/design/frontend-api.md`。

## Stack

- React
- TypeScript
- Vite
- Browser `fetch` API
- Same-origin API base：`/api`
- Vite dev proxy：`/api -> http://localhost:8136`

## Commands

```bash
cd /d/WWWWokP/Chlobot/frontend
npm install
npm run dev
npm run build
npm run preview
```

默认端口：

- Dev server：`http://localhost:5173`
- Preview server：`http://localhost:5174`
- Backend API：`http://localhost:8136/api`

## Route Coverage

| 页面 | Hash route | 状态 | 后端接口 |
| --- | --- | --- | --- |
| 概览 | `#/dashboard` | 已实现 | `/actuator/health`, `/observability/metrics/summary`, `/model/channels` |
| 会话 | `#/sessions` | 已实现创建/列表/消息读写 | `/sessions`, `/sessions/{sessionId}/messages` |
| 任务 | `#/tasks` | 已实现创建、详情、timeline、result 查询 | `/agent/tasks`, `/agent/tasks/{taskId}`, `/timeline`, `/result` |
| RAG | `#/rag` | 已实现导入/搜索 | `/rag/documents`, `/rag/search` |
| 记忆 | `#/memories` | 已实现创建/查询/prompt context 预览 | `/memories`, `/memories/prompt-context` |
| DAG | `#/graphs` | 已实现图写入/读取/ASCII/ready nodes/execute | `/agent/graphs`, `/execute`, `/ready-nodes`, `/ascii` |
| 观测 | `#/observability` | 已实现 summary 与 trace 查询 | `/observability/metrics/summary`, `/observability/tasks/{taskId}/trace` |
| 模型通道 | `#/model-channels` | 已实现通道健康与 chat smoke | `/model/channels`, `/chat`, `/actuator/prometheus` |
| 工具风险 | `#/tool-risk` | 已实现 Spring AI 工具描述与确认入口说明 | `/agent/tools/spring-ai/plan-ack`, `/agent/tasks/{taskId}/tool-calls` |
| 研究监控 | `#/observability/research` | 占位 | 后续 multi-agent / deep research change |
| 上下文预算 | `#/observability/context-budget` | 占位 | 后续 bootstrap context loading change |

## Project Layout

```text
frontend/
  index.html
  package.json
  vite.config.ts
  tsconfig.json
  tsconfig.node.json
  src/
    App.tsx
    main.tsx
    styles.css
    vite-env.d.ts
    api/
      client.ts
      types.ts
```

## API Client

`src/api/client.ts` 固定使用 `/api` 作为 base path，适配两种运行方式：

1. Vite 开发模式：`vite.config.ts` 将 `/api` 代理到 `http://localhost:8136`。
2. 生产部署：React 构建产物可由任意 Web server 或后续 Spring 静态资源集成方案托管，仍保持同源 `/api`。

## Verification

已验证：

```bash
cd /d/WWWWokP/Chlobot/frontend
npm install
npm run build
```

构建产物输出到 `frontend/dist/`。
