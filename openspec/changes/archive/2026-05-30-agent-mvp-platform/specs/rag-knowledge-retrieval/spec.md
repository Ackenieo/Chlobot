## ADDED Requirements

### Requirement: Document ingestion
The system SHALL allow clients to submit documents for knowledge ingestion.

#### Scenario: Import document
- **WHEN** a client submits a document with title, content, source URI, and metadata
- **THEN** the system stores the document and creates one or more retrievable chunks or embeddings

### Requirement: Hybrid retrieval
The system SHALL support vector, keyword, and hybrid retrieval modes for knowledge search.

#### Scenario: Perform hybrid search
- **WHEN** a client searches with hybrid mode and a top-k value
- **THEN** the system returns ranked results with content, document identifiers, scores, and metadata

### Requirement: Retrieval provenance
The system SHALL preserve source metadata for each retrieved knowledge item.

#### Scenario: Return source metadata
- **WHEN** a search result is returned
- **THEN** the response includes enough provenance to explain where the content came from
