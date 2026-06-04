# agent-task-graph-orchestration Specification

## Purpose
TBD - created by archiving change agent-mvp-platform. Update Purpose after archive.
## Requirements
### Requirement: Write task graph plan
The system SHALL allow clients or planners to write a DAG task graph for an Agent task.

#### Scenario: Create graph plan
- **WHEN** a client submits nodes and edges for an existing Agent task
- **THEN** the system persists graph nodes, graph edges, computes execution order, and returns parallel groups

### Requirement: Query ready nodes
The system SHALL expose nodes that are ready to execute based on dependency completion.

#### Scenario: Get ready nodes
- **WHEN** a graph contains pending nodes whose dependencies are all completed
- **THEN** the system returns those nodes ordered by priority

### Requirement: Claim node atomically
The system SHALL allow a worker to claim a pending ready node without duplicate assignment.

#### Scenario: Claim available node
- **WHEN** a worker claims a pending node with satisfied dependencies
- **THEN** the system changes the node status to `IN_PROGRESS`, records the owner, and emits a node-started event

#### Scenario: Reject duplicate claim
- **WHEN** a second worker claims a node that is already in progress
- **THEN** the system rejects the claim and leaves the original owner unchanged

### Requirement: Complete node and unlock dependents
The system SHALL complete nodes, persist results, and unlock downstream nodes when dependencies are satisfied.

#### Scenario: Complete node
- **WHEN** a worker completes a claimed node with a result
- **THEN** the system stores the result, changes the node status to `COMPLETED`, and emits node-completed and node-ready events for newly ready dependents

### Requirement: Execute graph in parallel batches
The system SHALL execute all ready nodes in parallel up to a configured concurrency limit.

#### Scenario: Execute independent nodes concurrently
- **WHEN** a graph has multiple ready nodes without dependencies between them
- **THEN** the system starts those nodes in the same execution batch and emits progress events for each node

### Requirement: Visualize graph status
The system SHALL expose a Java graph status view covering ready, blocked, in-progress, completed, and failed nodes.

#### Scenario: Query graph status
- **WHEN** a client requests graph status
- **THEN** the response includes total, ready, blocked, in-progress, completed, failed counts and node dependency details

