## Context

This follow-up covers onboarding and long-term context management as Java/Spring features. It should confirm profile facts before writing them into memory and store archive snapshots separately from current context.

## Goals / Non-Goals

**Goals:**
- Ask onboarding questions when a new user or session requires it.
- Persist only confirmed profile facts.
- Archive full conversation history before context compression.
- Store event journals, raw message archives, compressed summary snapshots, and replay metadata separately from live context.
- Restore context by session, task, archive snapshot, or event cursor.
- Support context backtracking/replay for debugging, continuation, and user review.

**Non-Goals:**
- No Python onboarding framework.
- No mandatory PDF export for MVP.

## Decisions

### 1. Memory confirmation first

Profile facts enter memory only after confirmation.

### 2. Archive is separate from live context

Use event journal + summary snapshots for restore.

### 3. Java export formats first

Markdown and JSON first; PDF can be another follow-up if needed.

## API Design

- `POST /onboarding/start`
- `POST /onboarding/confirm`
- `GET /context/archive/{sessionId}`
- `POST /context/restore`

## Data Model

- `onboarding_session`
- `user_profile_fact`
- `context_archive_snapshot`
