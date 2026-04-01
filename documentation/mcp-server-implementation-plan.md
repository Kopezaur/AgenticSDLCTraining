# MCP Server - Implementation Plan

## Overview

Build a Model Context Protocol (MCP) server as a separate Spring Boot application that allows AI agents (Claude, GPT-4o, etc.) to interact with the Task Manager database. The MCP server acts as a controlled access layer — it is **not** part of the main application flow and is used only by the agent to read/write data.

The server must comply with the [MCP specification version 2025-06-18](https://modelcontextprotocol.io/specification/2025-06-18).

## Architecture

```
[ AI Agent (e.g. Claude, GPT-4o) ]
              ⇅  (MCP protocol over stdio/SSE)
[ MCP Server (Spring Boot, port 8081) ]
              ⇅  (JDBC)
[ PostgreSQL DB (shared with Task Backend) ]
```

Key decisions:
- The MCP server lives in a separate `mcp-server/` folder within the same repository
- Both the existing backend and the MCP server share the same PostgreSQL database
- The existing backend must be migrated from H2 to PostgreSQL
- Database migrations are managed via Flyway scripts in the MCP server

## Tech Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| Framework | Spring Boot 3.x (Java 17) | Required by assignment, modern Spring features |
| MCP SDK | [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot.html) | Official Spring Boot MCP server support |
| Database | PostgreSQL | Shared with Task Backend, required by assignment |
| Migrations | Flyway | Versioned, repeatable DB migrations |
| Build Tool | Maven | Consistent with existing backend |
| Documentation | SpringDoc OpenAPI (optional) | Swagger UI for debugging |

## Database Migration: H2 to PostgreSQL

The existing backend currently uses H2 in-memory. Both applications need to share a PostgreSQL instance.

### Changes to existing backend

1. **`backend/pom.xml`** — Replace H2 dependency with PostgreSQL driver; add Flyway
2. **`backend/src/main/resources/application.properties`** — Update datasource to PostgreSQL:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
   spring.datasource.username=taskuser
   spring.datasource.password=taskpass
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=validate
   ```
3. **`backend/pom.xml`** — Upgrade to Spring Boot 3.x and Java 17 to align with MCP server requirements
4. **`backend/src/main/java/`** — Update `javax.persistence` imports to `jakarta.persistence` (required by Spring Boot 3)

### Flyway migration scripts

Located in `mcp-server/src/main/resources/db/migration/`:

| Script | Purpose |
|--------|---------|
| `V1__create_tasks_table.sql` | Creates the `tasks` table matching the existing Task entity |
| `V2__seed_sample_tasks.sql` | Inserts a small set of sample tasks for development/testing |

#### V1 — Create tasks table

```sql
CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date    DATE,
    CONSTRAINT chk_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'))
);
```

#### V2 — Seed sample data

```sql
INSERT INTO tasks (title, description, status, due_date) VALUES
('Set up CI pipeline', 'Configure GitHub Actions for build and test', 'TODO', '2026-04-15'),
('Write unit tests', 'Cover service layer with JUnit tests', 'IN_PROGRESS', '2026-04-10'),
('Deploy to staging', 'Push the current build to the staging environment', 'TODO', '2026-04-20'),
('Code review PR #42', 'Review the authentication module changes', 'DONE', '2026-04-01'),
('Update API docs', 'Refresh Swagger definitions for new endpoints', 'TODO', NULL);
```

## MCP Server Implementation

### Project Structure

```
mcp-server/
  pom.xml
  README.md
  src/main/java/com/taskmanager/mcp/
    McpServerApplication.java
    model/
      Task.java
      TaskStatus.java
    repository/
      TaskRepository.java
    tools/
      SchemaTools.java
      TaskTools.java
      HelpTools.java
    dto/
      TaskSummary.java
  src/main/resources/
    application.properties
    db/migration/
      V1__create_tasks_table.sql
      V2__seed_sample_tasks.sql
```

### Step 1: Initialize the Spring Boot project

Create a new Maven project under `mcp-server/` with dependencies:
- `spring-ai-starter-mcp-server` — MCP server auto-configuration
- `spring-boot-starter-data-jpa` — Repository layer
- `org.postgresql:postgresql` — PostgreSQL driver
- `org.flywaydb:flyway-core` + `flyway-database-postgresql` — Database migrations
- `springdoc-openapi-starter-webmvc-ui` (optional) — Swagger UI

### Step 2: Configure application properties

`mcp-server/src/main/resources/application.properties`:
```properties
spring.application.name=mcp-server
server.port=8081

# PostgreSQL (shared with backend)
spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
spring.datasource.username=taskuser
spring.datasource.password=taskpass
spring.jpa.hibernate.ddl-auto=validate

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# MCP
spring.ai.mcp.server.name=task-manager-mcp
spring.ai.mcp.server.version=1.0.0
```

### Step 3: Create the data model

Reuse the same entity structure as the existing backend, updated for Jakarta:

- **`Task.java`** — JPA entity mapped to the `tasks` table (same fields: id, title, description, status, dueDate)
- **`TaskStatus.java`** — Enum: `TODO`, `IN_PROGRESS`, `DONE`
- **`TaskRepository.java`** — Extends `JpaRepository<Task, Long>` with a custom count-by-status query

### Step 4: Implement MCP tools

Each tool is a Spring `@Service` with methods annotated using `@Tool` from the Spring AI MCP SDK.

#### `mcp-schema-tasks` — Schema inspection

```java
@Tool(name = "mcp-schema-tasks", description = "Returns the database schema for the tasks table as a simplified JSON Schema")
public String getTaskSchema() { ... }
```

Returns a hardcoded JSON Schema describing the tasks table:
```json
{
  "table": "tasks",
  "columns": {
    "id": { "type": "bigint", "primaryKey": true, "autoGenerated": true },
    "title": { "type": "varchar(100)", "required": true },
    "description": { "type": "varchar(500)", "required": false },
    "status": { "type": "varchar(20)", "enum": ["TODO", "IN_PROGRESS", "DONE"], "default": "TODO" },
    "due_date": { "type": "date", "required": false }
  }
}
```

#### `mcp-tasks` — Bulk insert

```java
@Tool(name = "mcp-tasks", description = "Accepts a JSON array of Task objects and inserts them into the database")
public String insertTasks(@ToolParam(description = "JSON array of task objects") List<Task> tasks) { ... }
```

- Accepts a list of Task objects
- Validates each task (title not blank, status valid)
- Uses `saveAll()` for batch insert
- Returns a summary: `{ "inserted": 1000, "failed": 0 }`

#### `mcp-tasks-summary` — Statistics

```java
@Tool(name = "mcp-tasks-summary", description = "Returns summary statistics including task counts per status")
public String getTasksSummary() { ... }
```

Returns:
```json
{
  "totalTasks": 1005,
  "byStatus": {
    "TODO": 400,
    "IN_PROGRESS": 350,
    "DONE": 255
  }
}
```

Uses a custom JPQL query: `SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status`

#### `mcp-help` — Agent-readable help

```java
@Tool(name = "mcp-help", description = "Returns a description of all available MCP tools")
public String getHelp() { ... }
```

Returns a plain-text or JSON listing of all tools with their names, descriptions, and parameter info.

### Step 5: Register tools with Spring AI MCP

Create a `@Configuration` class that provides a `ToolCallbackProvider` bean exposing all tool services to the MCP server runtime. This makes the tools discoverable and callable by connected AI agents.

### Step 6: Write tests

- **Tool unit tests** — Verify each tool method returns the expected output format
- **Integration test** — Start the MCP server, connect to an in-memory database, and verify tool registration

---

## Documentation Updates

### Root `README.md`

Add a new section covering:
- MCP server purpose and architecture
- PostgreSQL setup instructions (create database `taskdb`, user `taskuser`)
- How to run the MCP server (`cd mcp-server && mvn spring-boot:run`)
- How to configure an AI agent to connect to the MCP server

### `mcp-server/README.md`

Dedicated README with:
- What the MCP server does
- Available tools and their descriptions
- Prerequisites (Java 17, PostgreSQL)
- Setup and run instructions
- Example prompts for AI agents
- Expected output from each tool

---

## Updated Project Structure

```
AgenticSDLCTraining/
  backend/                        # Existing Task Manager API (updated to PostgreSQL)
    pom.xml                       # Updated: PostgreSQL driver, Spring Boot 3, Java 17
    src/main/resources/
      application.properties      # Updated: PostgreSQL connection
  frontend/                       # Existing React frontend (no changes)
  mcp-server/                     # NEW: MCP Server
    pom.xml
    README.md
    src/main/java/com/taskmanager/mcp/
      McpServerApplication.java
      model/
        Task.java
        TaskStatus.java
      repository/
        TaskRepository.java
      tools/
        SchemaTools.java
        TaskTools.java
        HelpTools.java
      dto/
        TaskSummary.java
    src/main/resources/
      application.properties
      db/migration/
        V1__create_tasks_table.sql
        V2__seed_sample_tasks.sql
    src/test/java/com/taskmanager/mcp/
      tools/
        SchemaToolsTest.java
        TaskToolsTest.java
  documentation/
    implementation-plan.md
    mcp-server-implementation-plan.md   # This file
    Agentic SDLC Advanced Tasks.pdf
  README.md                       # Updated with MCP server section
  .gitignore                      # Updated with mcp-server/target/
```

---

## Implementation Order

| Step | Task | Estimated Complexity |
|------|------|---------------------|
| 1 | Set up PostgreSQL locally, create `taskdb` database | Low |
| 2 | Upgrade existing backend: Spring Boot 3, Java 17, PostgreSQL | Medium |
| 3 | Initialize `mcp-server/` Maven project with dependencies | Low |
| 4 | Create Flyway migration scripts (V1, V2) | Low |
| 5 | Create Task entity, enum, and repository in MCP server | Low |
| 6 | Implement `mcp-schema-tasks` tool | Low |
| 7 | Implement `mcp-tasks` tool (bulk insert) | Medium |
| 8 | Implement `mcp-tasks-summary` tool | Low |
| 9 | Implement `mcp-help` tool | Low |
| 10 | Register tools via `ToolCallbackProvider` configuration | Low |
| 11 | Write tests | Medium |
| 12 | Create `mcp-server/README.md` | Low |
| 13 | Update root `README.md` with MCP server section | Low |
| 14 | Update `.gitignore` for `mcp-server/target/` | Low |
| 15 | Test end-to-end: connect AI agent, insert 1000 tasks, verify summary | Medium |

---

## Success Criteria

- [ ] MCP server starts on port 8081 and is accessible via MCP protocol (spec version 2025-06-18)
- [ ] `mcp-schema-tasks` returns accurate JSON Schema for the tasks table
- [ ] `mcp-tasks` successfully inserts 1000 task records in a single call
- [ ] `mcp-tasks-summary` returns correct counts per status after insert
- [ ] `mcp-help` lists all available tools with descriptions
- [ ] Flyway migrations run on startup and create the schema
- [ ] Existing backend works with PostgreSQL (no regressions)
- [ ] `mcp-server/README.md` documents usage and example prompts
- [ ] Root `README.md` includes MCP server setup instructions
