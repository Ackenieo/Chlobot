## ADDED Requirements

### Requirement: Java multi-agent modes
The system SHALL support two Java/Spring AI multi-agent orchestration modes: `SWARM` and `SUPERVISOR`.

#### Scenario: Select Swarm mode
- **WHEN** a multi-agent task is created with mode `SWARM`
- **THEN** the system creates peer agent assignments according to the Java role registry and Swarm policy

#### Scenario: Select Supervisor mode
- **WHEN** a multi-agent task is created with mode `SUPERVISOR`
- **THEN** the system creates a supervisor assignment and worker assignments controlled by the supervisor policy

### Requirement: Java Agent Role Registry
The system SHALL define agent roles through Java services and Spring AI Prompt/Advisor configuration rather than non-Java Agent runtimes.

#### Scenario: Resolve agent role
- **WHEN** a task requests a role such as planner, researcher, reviewer, verifier, synthesizer, coder, or tool-operator
- **THEN** the Java role registry returns the role prompt/advisor, allowed tools, memory/RAG policy, model channel policy, and output schema

### Requirement: Swarm peer collaboration
The system SHALL execute Swarm tasks as peer agent collaboration with shared goal, artifact exchange, structured review, voting, and synthesis.

#### Scenario: Swarm synthesis
- **WHEN** peer agents finish their assigned exploration or review work
- **THEN** the system collects structured artifacts/votes and produces a synthesized final result with traceable source artifacts

### Requirement: Supervisor controlled execution
The system SHALL execute Supervisor tasks through a supervisor agent that decomposes work, assigns worker roles, monitors progress, applies quality gates, and synthesizes the final result.

#### Scenario: Supervisor retries failed worker output
- **WHEN** a worker output fails supervisor quality checks
- **THEN** the supervisor records a decision and either retries, requests review, escalates to user confirmation, or marks the node failed according to policy

### Requirement: Reuse Java governance and task graph
The system SHALL reuse the existing Java task state machine, DAG executor, SSE event stream, tool governance, memory, RAG, and model channel governance for multi-agent execution.

#### Scenario: Worker invokes a tool
- **WHEN** any Swarm or Supervisor role invokes a tool
- **THEN** the invocation passes through the existing Java permission checks, confirmation rules, timeout, idempotency policy, and audit logging

### Requirement: Multi-agent observability
The system SHALL persist and stream multi-agent assignments, messages, artifacts, votes, reviews, supervisor decisions, and final synthesis events.

#### Scenario: Replay multi-agent timeline
- **WHEN** a client requests a multi-agent task event stream or timeline replay
- **THEN** the system returns ordered events showing role assignment, progress, artifacts, reviews, votes, decisions, and completion status
