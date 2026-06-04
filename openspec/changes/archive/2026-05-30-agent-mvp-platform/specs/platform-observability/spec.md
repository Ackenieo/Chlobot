## ADDED Requirements

### Requirement: Health and readiness
The system SHALL expose health information suitable for container health checks and API verification.

#### Scenario: Health check succeeds
- **WHEN** the application and required dependencies are available
- **THEN** the health endpoint reports an up status

### Requirement: Observability timeline
The system SHALL expose task timeline information for debugging and user feedback.

#### Scenario: Retrieve task timeline
- **WHEN** a client requests the timeline for a task
- **THEN** the system returns ordered events, status transitions, and notable errors

### Requirement: Metrics summary
The system SHALL expose aggregated metrics for task execution and platform operations.

#### Scenario: Return metrics summary
- **WHEN** a client requests the metrics summary endpoint
- **THEN** the system returns counts and latency/cost indicators that can be used for monitoring

### Requirement: Frontend observability console
The system SHALL provide frontend visualization pages for platform observability, task execution tracing, model channel health, RAG quality, tool risk, memory/rule hits, and multi-agent/research timelines.

#### Scenario: View observability dashboard
- **WHEN** an operator opens the observability console
- **THEN** the frontend displays service health, QPS, error rate, latency percentiles, active tasks, model channel state, token/cost indicators, and recent incidents

### Requirement: Agent execution trace visualization
The system SHALL expose and visualize Agent task execution traces including state transitions, DAG nodes, tool calls, model calls, RAG retrieval, memory injection, errors, retries, and final result.

#### Scenario: Inspect task trace
- **WHEN** an operator opens a task trace page
- **THEN** the frontend renders a timeline and graph view backed by persisted task events and trace metadata

### Requirement: Prompt token and cost observability
The system SHALL record prompt token estimates, completion token estimates, model latency, retries, fallback usage, and cost estimates per provider/model/channel/task mode.

#### Scenario: View model cost panel
- **WHEN** an operator filters metrics by provider, model, channel, or task mode
- **THEN** the system returns token, latency, error, retry, fallback, and cost metrics for visualization

### Requirement: RAG quality monitoring
The system SHALL expose RAG retrieval quality metrics and visualize query optimization, recall metrics, citation coverage, empty retrieval rate, retrieval latency, rerank/MMR behavior, and regression gate status.

#### Scenario: View RAG quality panel
- **WHEN** an operator opens the RAG quality dashboard
- **THEN** the frontend shows retrieval metrics, evaluation runs, baseline-vs-optimized comparison, failed gates, and recent low-quality queries

### Requirement: Multi-agent and research monitoring
The system SHALL visualize multi-agent and research workflow progress including role assignments, Swarm/Supervisor decisions, ToT branches, research tracks, reflection checks, artifacts, votes, and report export status.

#### Scenario: Monitor deep research task
- **WHEN** a deep research task is running
- **THEN** the frontend streams and renders expert roles, parallel tracks, reflection checkpoints, report sections, and export progress

### Requirement: Memory and behavior-rule monitoring
The system SHALL expose and visualize memory candidate extraction, confirmed profile facts, pitfall matches, behavior rule application, advisor injection trace, and rule conflicts.

#### Scenario: Inspect rule application
- **WHEN** a task uses a behavior rule or pitfall memory
- **THEN** the frontend shows which rule matched, why it matched, and which source memories produced it

### Requirement: Tool risk and audit monitoring
The system SHALL expose and visualize dangerous tool confirmations, approvals, rejections, timeouts, retries, idempotency decisions, and audit records.

#### Scenario: View tool risk panel
- **WHEN** an operator opens the tool risk dashboard
- **THEN** the frontend shows pending confirmations, recent decisions, risky tool frequency, failures, and audit links
