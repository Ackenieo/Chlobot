# Chlobot

> Production-ready Java Agent task orchestration platform built on Spring Boot & Spring AI.

[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-brightgreen)]()
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue)]()
[![License]([https://img.shields.io/badge/](http://www.apache.org/licenses/))]()

## Overview

Chlobot is an enterprise-grade AI Agent platform that emphasizes **deliverability, observability, governance, and evolution** — not just connecting LLMs to tools. It provides a complete task orchestration system with state machines, RAG knowledge retrieval, governed tool execution, multi-model routing, self-evolving memory, and full observability.

## Features

### Completed

| Module | Capability | Description |
|--------|-----------|-------------|
| **Task Platform** | State Machine & Event Stream | Task lifecycle management with explicit states (PENDING → ANALYZING → PLANNING → EXECUTING → COMPLETED/FAILED), SSE event streaming, pause/resume/cancel support |
| **DAG Orchestration** | Task Graph Execution | MySQL-persisted task graphs with dependency unlocking, transactional node claiming, `CompletableFuture` parallel execution |
| **Session & Message** | Conversation Management | Session creation, message history, multi-turn conversation tracking |
| **Tool Governance** | Confirmation & Audit | Tool registration with permission levels (L0-L5), dangerous operation human confirmation, idempotency, full audit logging |
| **Memory Management** | Self-Evolving Memory | MySQL-persisted user facts, preferences, project knowledge, experience lessons with CRUD operations |
| **RAG Retrieval** | Knowledge Base | Document ingestion (PDF/MD/TXT), pgvector embedding, hybrid search (vector + BM25), citation tracking |
| **Model Channel** | Multi-Model Routing | Spring AI ChatModel wrapper with priority routing, circuit breaker, fallback, retry governance |
| **Observability** | Prometheus + Grafana | LLM call metrics, tool call latency, task success rate, cost tracking, real-time dashboard |
| **Traffic Console** | Health Monitoring | First-token latency, model channel health, throughput monitoring |
| **Frontend** | React + TypeScript | Real-time task tracing, DAG visualization, monitoring dashboard, chat interface |

### Planned

| Module | Capability | Description |
|--------|-----------|-------------|
| **Multi-Agent Orchestration** | Swarm & Supervisor | Leader-Follower pattern, task decomposition, parallel agent collaboration, vote synthesis |
| **Research Workflow** | Deep Research | Plan confirmation, parallel research tracks, reflection verification, SSE report streaming |
| **Research Session Resume** | State Recovery | Checkpoint-based task recovery, event replay, intermediate state restoration |
| **Bootstrap Context** | Dynamic Assembly | Token budget management, dynamic context loading, Java caching strategies |
| **RAG Query Optimization** | Advanced Retrieval | Coreference resolution, Step-Back abstraction, query decomposition, multi-query rewriting, Rerank/MMR |
| **MCP Integration** | Tool Discovery | MCP server registration, tool discovery, Streamable HTTP/SSE transport |
| **Onboarding & Context Archive** | User Profiling | User onboarding flow, context archiving, preference learning |
| **Self-Evolving Knowledge** | Continuous Learning | Failure sample accumulation, automatic evaluation case generation, knowledge quality optimization |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
│  Chat UI · Task Tracing · DAG Viz · Monitoring Dashboard │
└────────────────────────┬────────────────────────────────┘
                         │ REST + SSE
┌────────────────────────▼────────────────────────────────┐
│                     API Layer                            │
│  Session · Message · Task · Tool · Memory · RAG · Health │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Orchestration Layer                     │
│  State Machine · DAG Executor · Event Stream · Recovery  │
└────────────────────────┬────────────────────────────────┘
                         │
┌───────────┬───────────┼───────────┬───────────┐
│ Agent Core│  Tools    │  Memory   │   RAG     │
│ LLM Call  │ Governance│ Self-Evo  │ pgvector  │
│ Prompt    │ Confirm   │ CRUD      │ Hybrid    │
│ Context   │ Audit     │ Profile   │ Search    │
└───────────┴───────────┴───────────┴───────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Storage Layer                          │
│         MySQL (Business) · PostgreSQL (Vector)           │
└─────────────────────────────────────────────────────────┘
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.3.7, Spring AI 1.0.0 |
| **Build** | Maven (Multi-module) |
| **Database** | MySQL 8.x (Business), PostgreSQL 16 + pgvector (Vector) |
| **Frontend** | React 18 + TypeScript + Vite |
| **Observability** | Micrometer + Prometheus + Grafana |
| **Deployment** | Docker Compose |

## Quick Start

### Prerequisites
- Docker Desktop
- JDK 21 (optional, for local dev)
- Maven 3.9+ (optional, for local dev)

### 1. Configure Application
```bash
cp app/src/main/resources/application.yml.example app/src/main/resources/application.yml
# Edit application.yml with your configuration
```

### 2. Start Services
```bash
# Windows
.\start.bat

# Linux/Mac
chmod +x start.sh && ./start.sh
```

### 3. Access Services
| Service | URL |
|---------|-----|
| API | http://localhost:8136/api |
| Swagger UI | http://localhost:8136/api/swagger-ui.html |
| Grafana | http://localhost:3000 |
| MySQL | localhost:3362 |
| PostgreSQL | localhost:5488 |

### 4. Test API
```bash
curl http://localhost:8136/api/actuator/health
```

## Project Structure

```
chlobot/
├── app/                    # Spring Boot application module
├── common/                 # Common utilities & API responses
├── agent-core/             # Agent core (LLM client, prompt builder)
├── agent-tools/            # Tool registration & governance
├── agent-memory/           # Self-evolving memory system
├── agent-rag/              # RAG knowledge retrieval
├── agent-model/            # Multi-model channel routing
├── agent-sandbox/          # Sandboxed execution environment
├── frontend/               # React + TypeScript frontend
├── docker/                 # Docker configurations
│   ├── grafana/            # Grafana dashboard JSON
│   ├── mysql/              # MySQL init scripts
│   └── postgres/           # PostgreSQL init scripts
├── dataset/                # SQL migration scripts
├── openspec/               # OpenSpec change proposals & specs
├── chlobot-compose.yml     # Docker Compose configuration
├── Dockerfile              # Application Docker image
└── start.bat / start.sh    # Startup scripts
```

## Design Philosophy

Chlobot follows these core principles:

1. **Business closure first, then intelligence** — Define users, tasks, inputs, outputs, and acceptance criteria before adding Agent capabilities.
2. **Deterministic flows before autonomy** — Use rules, state machines, and workflows where possible; reserve LLM for judgment and generation.
3. **LLM judges & generates, system constrains & executes** — Models can plan and select tools, but permissions, state, persistence, and auditing are system-controlled.
4. **Agent is a product capability, not a chat window** — Must be monitorable, replayable, pausable, resumable, and optimizable.
5. **Default distrust of model output** — Key operations require validation, confirmation, structured parsing, sandbox execution, or human approval.

See [signed/agent-project-design-philosophy.md](signed/agent-project-design-philosophy.md) for the complete design document.

## Observability

Chlobot provides comprehensive monitoring:

- **LLM Metrics**: Token usage, latency, cost per call
- **Tool Metrics**: Call success rate, latency, confirmation rate
- **Task Metrics**: Success rate, cancellation rate, retry rate
- **System Metrics**: CPU, memory, thread pool status
- **Business Metrics**: User satisfaction, intervention rate

Dashboard available at Grafana: http://localhost:3000

## Security

- Tool permission levels (L0-L5) with human confirmation for L3+
- Sandboxed command execution
- Audit logging for all tool calls
- User data isolation
- Sensitive data masking
- Memory edit/delete support

## API Documentation

Interactive API documentation available at:
- **Swagger UI**: http://localhost:8136/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8136/api/v3/api-docs

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing-feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue on GitHub.
