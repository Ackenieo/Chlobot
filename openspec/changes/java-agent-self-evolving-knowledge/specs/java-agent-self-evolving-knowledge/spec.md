## ADDED Requirements

### Requirement: Memory candidate extraction
The system SHALL extract self-evolving knowledge candidates from conversation events, task events, tool results, user corrections, and reflection outputs using Java/Spring AI services.

#### Scenario: Extract candidate from correction
- **WHEN** a user corrects an Agent plan or output
- **THEN** the system creates a memory candidate with source event, candidate type, confidence score, and confirmation status

### Requirement: Controlled memory lifecycle
The system SHALL manage self-evolving knowledge through explicit lifecycle states such as observed, candidate, inferred, confirmed, rule, and deprecated.

#### Scenario: Prevent inferred fact from becoming active profile
- **WHEN** a candidate is only inferred by the model and has not been confirmed
- **THEN** the system does not inject it as an active user profile fact

### Requirement: User profile facts
The system SHALL support confirmed user profile facts for role, expertise, language preference, tech stack preference, communication style, decision preference, risk tolerance, tool preferences, and confirmed constraints.

#### Scenario: Confirm profile fact
- **WHEN** a user confirms a proposed profile fact
- **THEN** the system persists it as a confirmed profile fact with source, timestamp, and confidence metadata

### Requirement: Pitfall memory
The system SHALL support pitfall memories that record trigger, mistake, correction, why, how to apply, related project, confidence, and source events.

#### Scenario: Reuse pitfall memory
- **WHEN** a future task matches a confirmed pitfall trigger
- **THEN** the system injects the pitfall correction through a Spring AI Advisor under the configured context budget

### Requirement: Behavior rule distillation
The system SHALL distill behavior rule candidates from repeated confirmed preferences, repeated corrections, and high-confidence pitfall memories.

#### Scenario: Distill behavior rule
- **WHEN** multiple confirmed memories imply a stable behavior pattern
- **THEN** the system creates a behavior rule candidate linked to its source memories and waits for confirmation or policy approval before activation

### Requirement: Advisor injection with trace
The system SHALL inject active user profile facts, preferences, pitfalls, and behavior rules into Agent context through Spring AI Advisor components with explicit budgets and trace logs.

#### Scenario: Trace applied rule
- **WHEN** a behavior rule is injected into a task context
- **THEN** the system records which rule was applied, why it matched, and which task used it

### Requirement: Knowledge governance
The system SHALL allow users to inspect, confirm, reject, edit, delete, disable, deprecate, and roll back self-evolving knowledge entries and behavior rules.

#### Scenario: Disable behavior rule
- **WHEN** a user disables a behavior rule
- **THEN** the system stops applying the rule to future Agent contexts and records the change in audit logs

### Requirement: Sensitive data protection
The system SHALL prevent secrets, credentials, tokens, passwords, and disallowed sensitive data from being stored as self-evolving knowledge.

#### Scenario: Reject secret candidate
- **WHEN** a memory candidate appears to contain a secret or credential
- **THEN** the system rejects or redacts the candidate according to security policy and records the reason
