## Why

Chlobot 已经具备基础 memory-management 与 `java-onboarding-context-archive` 用户确认画像规划，但还缺少完整的 Agent 自我进化知识库：Agent 需要能在对话中发现候选记忆、记录用户信息与偏好、沉淀踩坑经验，并在确认和治理后自动提炼为可注入后续行为的规则。

本 change 将该能力作为 Java/Spring AI follow-up：使用 MySQL 持久化、Spring AI Advisor/Prompt 注入、Java 服务治理和用户确认机制实现，不引入非 Java 记忆框架作为主链路。

## What Changes

- 新增 Agent self-evolving knowledge base 规划。
- 定义记忆类型：用户画像、用户偏好、项目偏好、工作流规则、踩坑经验、纠错、确认事实、行为规则、工具使用规则、领域知识。
- 支持从对话、任务事件、工具结果和用户纠错中提取 memory candidates。
- 支持候选记忆分类、置信度评分、去重、合并、过期和用户确认。
- 支持 confirmed user profile，记录角色、技术栈偏好、语言偏好、沟通偏好、风险偏好和项目约束。
- 支持 pitfall memory，沉淀触发条件、错误做法、修正方式、原因和应用方式。
- 支持将多次确认的偏好/纠错/踩坑经验提炼为 behavior rules。
- 支持通过 Spring AI Advisor 将用户画像、项目偏好、踩坑经验和行为规则注入 Agent 上下文。
- 支持记忆治理：编辑、删除、禁用、溯源、审计、冲突检测和规则回滚。

## Non-Goals

- 不允许 Agent 无限制写入永久记忆。
- 不把 inferred memory 当作 confirmed fact 使用。
- 不保存敏感信息、密钥、凭证或隐私数据，除非用户明确授权且通过安全策略。
- 不绕过用户确认写入长期用户画像。

## Capabilities

- `java-agent-self-evolving-knowledge`: Java/Spring AI Agent 自我进化知识库、用户画像、踩坑经验和行为规则提炼能力。

## Impact

- `agent-memory`: 增加 memory candidate、profile fact、pitfall、behavior rule、confidence、source 和 lifecycle 模型。
- `agent-core`: 在任务完成、用户纠错、确认和反思阶段触发候选记忆提取与规则应用。
- `agent-model`: 增加 Spring AI Advisor，用于注入 profile、preference、pitfall 和 behavior rules。
- `app`: 增加候选记忆确认、用户画像、规则管理和记忆审计 API。
- 数据库：新增或扩展 MySQL 表，用于候选记忆、用户画像、行为规则、规则应用日志和审计。
