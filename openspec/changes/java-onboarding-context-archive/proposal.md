## Why

The product highlight set includes onboarding, confirmed user profile extraction, and context archive/restore. These should be implemented as a Java follow-up change, not as hidden assumptions inside the MVP.

## What Changes

- Add a Java onboarding flow for first conversation or new user setup.
- Confirm profile facts before storing them as memory.
- Provide Java context archive and restore APIs using full conversation archive, event journals, compression snapshots, replay metadata, and restore cursors.
- Archive complete conversation history before compression and keep compressed summary snapshots separately.
- Support context rewind/backtracking by session, task, snapshot, or event cursor.
- Keep the capability aligned with Spring AI memory/context assembly.

## Capabilities

- `java-onboarding-context-archive`: Java onboarding, profile extraction, and context archive/restore.

## Impact

- Code structure: onboarding flow, profile extractor, archive snapshot service.
- API: onboarding and archive/restore endpoints.
- Dependency: Spring AI memory/context support, Java persistence, export helpers.
