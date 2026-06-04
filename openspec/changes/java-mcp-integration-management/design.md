## Context

This change is a Java-only follow-up for MCP integration. If MCP support is not available through Spring AI or a Java HTTP/SSE adapter, the project should defer the feature further rather than pulling in non-Java runtime dependencies.

## Goals / Non-Goals

**Goals:**
- Register and manage MCP server metadata.
- Discover tools from MCP servers.
- Route tool calls through the existing Java confirmation and audit model.
- Support streamable transport where Java support exists.

**Non-Goals:**
- No non-Java runtime dependency.
- No bypass of tool governance.
- No requirement to make MCP part of MVP task execution.

## Decisions

### 1. Governance first

All MCP tools are treated as ordinary governed tools once discovered.

### 2. Java transport only

Use Spring AI MCP support or Java HTTP/SSE client/server code only.

### 3. Optional runtime integration

MCP may remain optional if the Java integration surface is too thin.

## API Design

- `GET /mcp/servers`
- `POST /mcp/servers`
- `POST /mcp/servers/{serverId}/start`
- `POST /mcp/servers/{serverId}/stop`
- `GET /mcp/servers/{serverId}/tools`
- `POST /mcp/servers/{serverId}/tools/{toolName}/execute`

## Data Model

- `mcp_server`
- `mcp_tool`
- `mcp_tool_execution`
