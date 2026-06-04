## 1. Resume State Model

- [ ] 1.1 Define research session, cursor, snapshot, and resume event DTOs
- [ ] 1.2 Add task-level resume metadata to research task lifecycle
- [ ] 1.3 Add cursor acknowledgment and replay token handling
- [ ] 1.4 Add idempotent resumed event delivery rules

## 2. Persistence and Recovery

- [ ] 2.1 Persist research task cursor and snapshot on each meaningful progress update
- [ ] 2.2 Persist active stage, ToT branch, track summary, reflection checkpoint, and export status in snapshot
- [ ] 2.3 Implement resume service that reconstructs current progress from snapshot plus delta events
- [ ] 2.4 Implement session attach/detach logic for active research tasks
- [ ] 2.5 Prevent duplicate event emission after reconnect

## 3. API and Verification

- [ ] 3.1 Add resume-state API and active-task query API
- [ ] 3.2 Add resume endpoint that returns snapshot, cursor, and pending delta events
- [ ] 3.3 Add SSE reconnect support using cursor query parameter
- [ ] 3.4 Verify session switch resume with curl
- [ ] 3.5 Verify disconnect/reconnect replays only missing events
