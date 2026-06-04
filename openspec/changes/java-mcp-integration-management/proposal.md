## Why

MCP should not be assumed as part of the MVP runtime, but the project needs a Java follow-up path so that MCP can be integrated only through Java/Spring AI-compatible adapters. This follow-up keeps the tool-governance model intact and avoids cross-language runtime dependency.

## What Changes

- Define a Java MCP registry and connection model.
- Support tool discovery only through Java HTTP/SSE or Spring AI-compatible integration points.
- Ensure every MCP-exposed tool still passes the existing Java tool governance flow.
- Keep MCP server lifecycle management separate from core task execution unless supported cleanly in Java.

## Capabilities

- `java-mcp-integration-management`: Java/Spring AI MCP registry, discovery, transport adapter, and governance integration.

## Impact

- Code structure: MCP registry service, transport adapter, discovered tool mapping.
- API: MCP server register/list/start/stop/tools/execution endpoints.
- Dependency: only Java/Spring AI-compatible MCP support or Java HTTP/SSE adapter.
