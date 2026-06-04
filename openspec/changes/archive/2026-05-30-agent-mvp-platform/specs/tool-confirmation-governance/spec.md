## ADDED Requirements

### Requirement: Tool registry metadata
The system SHALL represent each Agent tool with name, description, input schema, output schema, permission level, read-only flag, confirmation requirement, timeout, retry policy, and idempotency metadata.

#### Scenario: List tool calls for task
- **WHEN** a client requests tool calls for a task
- **THEN** the system returns persisted tool call records including status, permission level, confirmation requirement, input, output, and timestamps

### Requirement: Human confirmation for risky tools
The system SHALL require explicit confirmation before executing tool calls marked as requiring confirmation.

#### Scenario: Tool call waits for confirmation
- **WHEN** an Agent requests a tool call that requires confirmation
- **THEN** the system persists the tool call, emits a confirmation-required event, and does not execute the side effect until approved

#### Scenario: Tool call is rejected
- **WHEN** a client rejects a pending tool call
- **THEN** the system records the rejection reason, emits a tool-call-rejected event, and resumes or fails the task according to policy

### Requirement: Tool audit trail
The system SHALL record an audit entry for every tool confirmation decision and executed tool call.

#### Scenario: Approved tool execution is audited
- **WHEN** a confirmed tool call is executed
- **THEN** the system records actor, action, target, input, output summary, and timestamp in the audit log
