## ADDED Requirements

### Requirement: Research session persistence
The system SHALL persist research progress, event cursors, and resume metadata so that a research task can be resumed after client disconnects or session switch.

#### Scenario: Resume after disconnect
- **WHEN** a client disconnects during an active research task
- **THEN** the system keeps the research state in persistence and exposes a resume point for later reconnection

### Requirement: Cursor-based SSE resume
The system SHALL support SSE reconnection using a cursor or replay token so that only missing research events are replayed.

#### Scenario: Reconnect with cursor
- **WHEN** a client reconnects with the last acknowledged cursor
- **THEN** the system replays only events after that cursor in the original order

### Requirement: Session switch recovery
The system SHALL allow a user to attach an active research task to a different session view and recover the current state from snapshot plus delta events.

#### Scenario: Attach task to new session
- **WHEN** a user opens a different session but requests the same active research task
- **THEN** the system returns the active stage, current cursor, snapshot summary, and pending events needed to continue viewing or controlling the task

### Requirement: Idempotent resumed delivery
The system SHALL ensure resumed event delivery is idempotent and does not duplicate already acknowledged events.

#### Scenario: Avoid duplicate replay
- **WHEN** the same cursor is acknowledged more than once or the client reconnects repeatedly
- **THEN** the system does not duplicate events that were already delivered and acknowledged
