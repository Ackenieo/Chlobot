## ADDED Requirements

### Requirement: Java MCP server registry
The system SHALL allow operators to register, list, start, stop, and delete MCP server metadata through Java services when a Java/Spring AI-compatible integration is selected.

#### Scenario: Register MCP server
- **WHEN** an operator submits MCP server connection metadata
- **THEN** the Java service persists the server and makes it available for tool discovery

### Requirement: Governed MCP tool discovery and execution
The system SHALL discover MCP tools through Java/Spring AI MCP support or Java HTTP/SSE adapters and execute discovered tools through the same Java tool governance layer.

#### Scenario: Execute MCP tool
- **WHEN** an Agent invokes a discovered MCP tool
- **THEN** the system applies permission checks, confirmation rules, execution timeout, and audit logging

### Requirement: Java streamable transport adapter
The system SHALL relay MCP Streamable HTTP/SSE progress through the existing task event stream when Java transport support is available.

#### Scenario: Relay streamable events
- **WHEN** an MCP server responds with streamable events
- **THEN** the Java adapter relays progress through the task SSE stream
