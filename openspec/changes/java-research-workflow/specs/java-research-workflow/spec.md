## ADDED Requirements

### Requirement: Java research lifecycle
The system SHALL support research tasks using Java task state machine, Java DAG executor, Spring AI, and SSE events.

#### Scenario: Research requires confirmation
- **WHEN** a research task is created
- **THEN** the system asks clarifying questions when needed, generates a plan, and waits for user confirmation before execution

### Requirement: Research mode selection
The system SHALL support at least two research modes: `STANDARD` and `DEEP`.

#### Scenario: Select deep mode
- **WHEN** a user creates a research task in `DEEP` mode
- **THEN** the system enables expert-agent collaboration, ToT planning, and deeper verification before final synthesis

### Requirement: ToT planning
The system SHALL support Tree-of-Thought style planning for deep research by producing multiple candidate branches and selecting or merging them under policy control.

#### Scenario: Generate candidate branches
- **WHEN** a deep research task enters planning
- **THEN** the system creates multiple candidate reasoning branches and records the chosen plan path

### Requirement: Expert agent collaboration
The system SHALL support expert-agent collaboration for deep research by integrating with the Java multi-agent orchestration layer.

#### Scenario: Assign expert roles
- **WHEN** a deep research task needs specialist perspectives
- **THEN** the system assigns expert roles such as lead researcher, domain expert, evidence analyst, skeptic verifier, and report writer

### Requirement: Parallel research tracks
The system SHALL execute independent research tracks in parallel while preserving ordered events inside each track.

#### Scenario: Execute multiple tracks
- **WHEN** a confirmed research plan contains independent tracks
- **THEN** the Java DAG executor runs those tracks concurrently and emits progress events per track

### Requirement: Reflection verification
The system SHALL support reflection checkpoints that validate evidence, identify gaps, and request additional research before final synthesis.

#### Scenario: Trigger reflection checkpoint
- **WHEN** a research track reaches a verification boundary
- **THEN** the system records reflection notes, evidence checks, and any follow-up actions needed

### Requirement: Streaming Java report generation
The system SHALL stream research report generation through SSE and support asynchronous Markdown/JSON export.

#### Scenario: Export report
- **WHEN** research completes and export is requested
- **THEN** the system creates a Java export job and returns downloadable report metadata when ready

### Requirement: PDF report export
The system SHOULD support PDF export for completed research reports using Java export tooling such as PDFBox or OpenHTMLToPDF.

#### Scenario: Export PDF report
- **WHEN** a completed report is exported in PDF format
- **THEN** the system generates a PDF artifact asynchronously and returns its download metadata when ready
