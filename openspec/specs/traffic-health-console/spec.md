# traffic-health-console Specification

## Purpose
TBD - created by archiving change agent-mvp-platform. Update Purpose after archive.
## Requirements
### Requirement: Traffic health console
The system SHALL expose a traffic health console API for runtime health visualization.

#### Scenario: Query traffic summary
- **WHEN** an operator requests the traffic health summary
- **THEN** the system returns QPS, error rate, P95/P99 latency, first-token latency, active task counts, and model channel status

### Requirement: Traffic health frontend visualization
The system SHALL provide a frontend traffic health page that visualizes QPS, error rate, P95/P99 latency, first-token latency, first-SSE-event latency, active tasks, model channel health, Sentinel rule status, and recent incidents.

#### Scenario: Open traffic health page
- **WHEN** an operator navigates to `/traffic-health`
- **THEN** the frontend renders real-time summary cards, time-series charts, channel table, Sentinel rule status, and incident list backed by Java monitoring APIs

The system SHALL measure first-token and first-SSE-event latency for streaming model responses.

#### Scenario: Record first token latency
- **WHEN** a model stream emits its first token
- **THEN** the system records first-token latency with provider, model, channel, task mode, and status labels

### Requirement: Prometheus and Grafana integration
The system SHALL export monitoring metrics to Prometheus and provide Grafana dashboard planning.

#### Scenario: Scrape metrics
- **WHEN** Prometheus scrapes the application metrics endpoint
- **THEN** first-token latency, model errors, task throughput, cache hit rate, and retrieval latency metrics are available

### Requirement: Sentinel dashboard deployment
The system SHALL include Sentinel Dashboard in Docker Compose for flow, degrade, and system rule management.

#### Scenario: Start Sentinel dashboard
- **WHEN** Docker Compose starts with observability services enabled
- **THEN** Sentinel Dashboard is reachable on the configured project-offset port

