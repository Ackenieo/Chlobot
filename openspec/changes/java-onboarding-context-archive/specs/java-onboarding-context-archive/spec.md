## ADDED Requirements

### Requirement: Java onboarding flow
The system SHALL support Java/Spring onboarding sessions for first conversation or explicit profile setup.

#### Scenario: Start onboarding
- **WHEN** a new user or session requires onboarding
- **THEN** the system creates an onboarding session and asks configured onboarding questions

### Requirement: Confirmed profile memory
The system SHALL persist user profile facts into memory only after user confirmation.

#### Scenario: Confirm profile fact
- **WHEN** a user confirms a proposed profile fact
- **THEN** the system stores it as a confirmed memory with source and timestamp

### Requirement: Context archive and restore
The system SHALL archive full session context using Java persistence and restore it from raw conversation archives, event journals, summary snapshots, and replay metadata.

#### Scenario: Restore context
- **WHEN** a user requests context restore for an archived session
- **THEN** the system returns the latest snapshot and replay metadata needed to continue the session

### Requirement: Full conversation archive before compression
The system SHALL archive complete conversation history before context compression, including messages, task events, tool calls, model calls, RAG citations, memory injections, and summary inputs.

#### Scenario: Archive before compression
- **WHEN** a session context is about to be compressed
- **THEN** the system stores the complete pre-compression history and links the compressed summary snapshot to its source archive

### Requirement: Context backtracking
The system SHALL support context backtracking by session, task, snapshot, or event cursor.

#### Scenario: Backtrack to archive point
- **WHEN** a user selects an archive snapshot or event cursor
- **THEN** the system returns the archived messages, summary snapshot, source events, and replay metadata for that point

### Requirement: Archive governance
The system SHALL apply retention, soft-delete, access audit, and sensitive data protection to archived context.

#### Scenario: Audit archive access
- **WHEN** a user or operator reads an archived context snapshot
- **THEN** the system records who accessed it, when it was accessed, and which archive metadata was returned
