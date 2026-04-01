# AgenticSDLCTraining

This repository contains the solution for the first task of the AI Core Heroes training.

It is a full-stack **Task Manager** application with CRUD operations, built with a Spring Boot backend and a React frontend. It also includes an **MCP Server** that allows AI agents to interact with the database.

## Prerequisites

- Java 17 and Maven
- Node.js (v16+) and npm
- PostgreSQL

## Database Setup

1. Install PostgreSQL and create the database and user:

```sql
CREATE USER taskuser WITH PASSWORD 'taskpass';
CREATE DATABASE taskdb OWNER taskuser;
```

2. The MCP server runs Flyway migrations on startup to create the schema automatically.

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

Open `http://localhost:5173` in your browser to use the application.

### MCP Server

```bash
cd mcp-server
mvn spring-boot:run
```

The MCP server starts on `http://localhost:8081`. It connects to the same PostgreSQL database as the backend and exposes tools for AI agents via the MCP protocol.

The SSE endpoint is available at `http://localhost:8081/sse`.

To connect an AI agent (e.g., Claude), add to your MCP configuration:

```json
{
  "mcpServers": {
    "task-manager": {
      "url": "http://localhost:8081/sse"
    }
  }
}
```

See [mcp-server/README.md](mcp-server/README.md) for available tools, example prompts, and detailed usage instructions.
