## 1. Research Task Lifecycle

- [ ] 1.1 Define research task states and transitions
- [ ] 1.2 Define `ResearchMode` enum with `STANDARD` and `DEEP`
- [ ] 1.3 Implement clarification and plan confirmation flow
- [ ] 1.4 Implement ToT planning with multiple candidate reasoning branches, scoring, and selected plan output
- [ ] 1.5 Implement parallel track execution on Java executor
- [ ] 1.6 Implement reflection checkpoints and event emission
- [ ] 1.7 Implement report generation and export

## 2. Expert Agent Collaboration

- [ ] 2.1 Integrate with `java-multiagent-orchestration` role registry for expert roles
- [ ] 2.2 Define default expert roles: lead researcher, domain expert, evidence analyst, skeptic verifier, report writer
- [ ] 2.3 Support Swarm-style peer research for DEEP mode when multiple expert perspectives are needed
- [ ] 2.4 Support Supervisor-style controlled research when a lead researcher must assign tracks and approve outputs
- [ ] 2.5 Persist expert assignments, artifacts, reviews, votes, and supervisor decisions with research events

## 3. Report Export

- [ ] 3.1 Stream report outline, section drafts, citations, reflection notes, and final synthesis through SSE
- [ ] 3.2 Support Markdown and JSON report export
- [ ] 3.3 Add Java PDF export using PDFBox or OpenHTMLToPDF after report HTML/Markdown rendering is stable
- [ ] 3.4 Include citations, evidence table, methodology, limitations, and reflection verification summary in exported reports

## 4. Verification

- [ ] 4.1 Verify STANDARD research task creation and confirmation with curl
- [ ] 4.2 Verify DEEP research task creates ToT plan branches and expert assignments
- [ ] 4.3 Verify parallel tracks emit events in order
- [ ] 4.4 Verify reflection verification events are persisted and replayable
- [ ] 4.5 Verify export endpoint returns completed Markdown/JSON/PDF report metadata
