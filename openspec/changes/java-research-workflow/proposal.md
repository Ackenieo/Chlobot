## Why

当前 MVP 计划保留了 Java DAG 任务图和基础任务编排，但真正的专家型深度研究系统需要一个独立的 Java follow-up change：使用 Java task state machine、DAG executor、Spring AI、SSE 和 `java-multiagent-orchestration` 支持标准/深度两种研究模式、ToT 规划、专家 Agent 协作、并行研究、反思验证、流式报告和导出。

## What Changes

- 定义 Java 研究任务的生命周期和状态流转。
- 支持 `STANDARD` / `DEEP` 两种研究模式。
- 支持 ToT(Tree-of-Thought) 多路径规划、候选分支评分和计划确认。
- 集成 `java-multiagent-orchestration`，支持专家型 Agent 协作。
- 复用现有任务图和并行执行能力表示研究 tracks。
- 支持澄清问题、计划确认、执行中事件流和结果报告。
- 支持反思验证、证据一致性检查和缺口追问。
- 研究报告支持流式生成与异步导出，优先 Markdown/JSON，并规划 Java PDF 导出（PDFBox/OpenHTMLToPDF）。

## Capabilities

- `java-research-workflow`: 基于 Java/Spring AI 的专家型深度研究任务编排，支持标准/深度模式、ToT 规划、专家协作、并行 track、反思验证、流式报告和 Markdown/JSON/PDF 导出。

## Impact

- 代码结构：新增 research task domain、executor、report service。
- API：新增 research task 创建、事件流、计划确认和导出接口。
- 依赖：Spring AI、SSE、Java 并发、可选导出库。
