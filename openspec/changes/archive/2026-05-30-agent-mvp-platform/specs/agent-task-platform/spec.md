## ADDED Requirements

### Requirement: Task creation and lifecycle
The system SHALL allow clients to create Agent tasks and track their lifecycle through a controlled state machine.

#### Scenario: Create task
- **WHEN** a client submits a valid task request with session ID and input
- **THEN** the system creates a task with status `PENDING` and returns a task ID and event stream URL

#### Scenario: State transition validation
- **WHEN** an invalid state transition is requested or attempted internally
- **THEN** the system rejects the transition and records an error event without corrupting the persisted task state

### Requirement: Plan confirmation
The system SHALL support plan generation and explicit confirmation before executing tasks that require confirmation.

#### Scenario: Task waits for plan confirmation
- **WHEN** a task requires plan confirmation and a plan is generated
- **THEN** the system persists the plan, changes task status to `PENDING_CONFIRMATION`, and emits a `plan_generated` event

#### Scenario: Confirm plan
- **WHEN** a client approves a pending plan
- **THEN** the system records the confirmation, changes task status to `CONFIRMED` or `EXECUTING`, and continues execution

### Requirement: Persistent event stream
The system SHALL persist task events and expose them through an SSE endpoint and a replay endpoint.

#### Scenario: Subscribe to task events
- **WHEN** a client opens the task event stream
- **THEN** the system emits task events as `text/event-stream` messages in sequence order

#### Scenario: Replay task timeline
- **WHEN** a client requests a task timeline after refresh or reconnect
- **THEN** the system returns previously persisted events for the task in sequence order

### Requirement: Cancel and retry task
The system SHALL allow clients to cancel active tasks and retry failed or cancelled tasks.

#### Scenario: Cancel active task
- **WHEN** a client cancels a task that is not completed
- **THEN** the system changes the task status to `CANCELLED` and emits a `cancelled` event

#### Scenario: Retry failed task
- **WHEN** a client retries a failed task
- **THEN** the system creates or resets execution state according to the retry policy and emits a retry event
