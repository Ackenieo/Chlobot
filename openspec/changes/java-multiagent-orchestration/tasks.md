## 1. Multi-agent Policy Model

- [ ] 1.1 Define `MultiAgentMode` enum with `SWARM` and `SUPERVISOR`
- [ ] 1.2 Define Java DTOs/records for `MultiAgentPolicy`, `AgentRole`, `AgentAssignment`, `AgentArtifact`, `AgentVote`, and `AgentDecision`
- [ ] 1.3 Define budget fields for max agents, max steps, max tokens, max tool calls, and timeout
- [ ] 1.4 Define output contracts with Java records, Bean Validation, Jackson, and OpenAPI annotations

## 2. Agent Role Registry

- [ ] 2.1 Add Java role registry service with fixed initial roles: planner, researcher, coder, reviewer, verifier, synthesizer, tool-operator
- [ ] 2.2 Bind each role to Spring AI Prompt/Advisor configuration and model channel policy
- [ ] 2.3 Bind each role to allowed tool categories through existing Java tool governance
- [ ] 2.4 Bind each role to memory/RAG access policy and context budget

## 3. Swarm Mode

- [ ] 3.1 Implement Swarm coordination plan that launches peer role assignments over shared goal or DAG branches
- [ ] 3.2 Implement cross-review / adversarial verification between peer agents
- [ ] 3.3 Implement structured vote/score collection with persisted `AgentVote`
- [ ] 3.4 Implement synthesis step that merges peer outputs into final result with citations to artifacts
- [ ] 3.5 Emit SSE events for peer start, artifact creation, review, vote, and synthesis

## 4. Supervisor Mode

- [ ] 4.1 Implement Supervisor planner that decomposes task into DAG nodes and worker assignments
- [ ] 4.2 Implement worker execution through existing Java DAG executor and Spring AI role adapters
- [ ] 4.3 Implement supervisor quality gates for retry, request-review, approve, fail, or escalate-to-user decisions
- [ ] 4.4 Persist supervisor decisions and link them to node events and artifacts
- [ ] 4.5 Emit SSE events for supervisor assignment, progress monitoring, decisions, retries, and final synthesis

## 5. Persistence and API

- [ ] 5.1 Add MySQL migration/init SQL for multi-agent sessions, assignments, messages, artifacts, votes, decisions, and reviews
- [ ] 5.2 Add REST APIs for creating/listing multi-agent tasks and reading artifacts
- [ ] 5.3 Add role registry management/listing API with admin guard or local-only guard for MVP follow-up
- [ ] 5.4 Add OpenAPI examples for both `SWARM` and `SUPERVISOR` task creation

## 6. Governance and Verification

- [ ] 6.1 Ensure all tool calls from all roles go through existing confirmation, timeout, idempotency, and audit rules
- [ ] 6.2 Add model channel metrics tagged by role, mode, task, channel, fallback, and status
- [ ] 6.3 Add curl verification for creating a `SWARM` task and replaying SSE events
- [ ] 6.4 Add curl verification for creating a `SUPERVISOR` task and confirming supervisor plan
- [ ] 6.5 Verify no Python-first multi-agent runtime is required
