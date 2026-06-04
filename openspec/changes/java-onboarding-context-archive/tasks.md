## 1. Onboarding

- [ ] 1.1 Implement onboarding session creation
- [ ] 1.2 Implement profile fact confirmation
- [ ] 1.3 Persist confirmed facts into memory

## 2. Archive / Restore

- [ ] 2.1 Implement full conversation archive creation before context compression
- [ ] 2.2 Persist raw messages, tool events, model calls, RAG citations, memory injections, and task events into archive storage
- [ ] 2.3 Implement compressed summary snapshot creation with source archive links
- [ ] 2.4 Implement restore from archive snapshot, session id, task id, or event cursor
- [ ] 2.5 Implement context backtracking/replay API for archived sessions
- [ ] 2.6 Add export format support for JSON/Markdown and later PDF when needed
- [ ] 2.7 Add retention, soft-delete, and access audit for archived context

## 3. Verification

- [ ] 3.1 Verify onboarding and confirm flow with curl
- [ ] 3.2 Verify archive and restore endpoints with curl
