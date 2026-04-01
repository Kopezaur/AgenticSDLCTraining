# Task Manager Web App - Implementation Plan

## Project Overview

A full-stack web application for managing tasks with CRUD operations (Create, Read, Update, Delete). Users can create tasks with a title, description, status, and optional due date, then view, edit, and delete them through a clean web interface.

## Tech Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Frontend | React 18 + TypeScript | Type safety, component-based UI |
| Frontend Build | Vite | Fast dev server and builds |
| Frontend Styling | TailwindCSS | Utility-first, minimal config |
| Backend | Spring Boot 2.7 (Java 11) | Mature REST framework with built-in validation |
| Backend Build | Maven | Standard Java build tool |
| Database | H2 (in-memory) | Zero setup, embedded, ideal for development |
| Communication | REST API (JSON) | Simple, well-understood contract |

## Data Model

### Task Entity

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | Long | Auto-generated primary key |
| `title` | String | Required, max 100 characters |
| `description` | String | Optional, max 500 characters |
| `status` | Enum | `TODO`, `IN_PROGRESS`, `DONE` (default: `TODO`) |
| `dueDate` | LocalDate | Optional |

## REST API Contract

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|-------------|----------|
| GET | `/api/tasks` | List all tasks | - | `200` + Task[] |
| GET | `/api/tasks/{id}` | Get task by ID | - | `200` + Task / `404` |
| POST | `/api/tasks` | Create a task | Task JSON | `201` + Task |
| PUT | `/api/tasks/{id}` | Update a task | Task JSON | `200` + Task / `404` |
| DELETE | `/api/tasks/{id}` | Delete a task | - | `204` / `404` |

### Example JSON

```json
{
  "id": 1,
  "title": "Complete implementation plan",
  "description": "Write the full plan for the Task Manager app",
  "status": "IN_PROGRESS",
  "dueDate": "2026-03-25"
}
```

### Validation Error Response (400)

```json
{
  "title": "must not be blank",
  "description": "size must be between 0 and 500"
}
```

---

## Project Structure

```
AgenticSDLCTraining/
  backend/
    pom.xml
    src/main/java/com/taskmanager/
      TaskManagerApplication.java
      model/
        Task.java
        TaskStatus.java
      repository/
        TaskRepository.java
      service/
        TaskService.java
      controller/
        TaskController.java
      config/
        CorsConfig.java
      exception/
        TaskNotFoundException.java
        GlobalExceptionHandler.java
    src/main/resources/
      application.properties
    src/test/java/com/taskmanager/
      controller/
        TaskControllerTest.java
      service/
        TaskServiceTest.java
  frontend/
    package.json
    tsconfig.json
    tailwind.config.js
    vite.config.ts
    index.html
    src/
      main.tsx
      App.tsx
      types/
        Task.ts
      services/
        taskApi.ts
      components/
        TaskList.tsx
        TaskForm.tsx
        TaskItem.tsx
```

---

## Backend Implementation

### Step 1: Initialize Spring Boot Project

Generate a Spring Boot 3 project with the following dependencies:
- **Spring Web** - REST controllers
- **Spring Data JPA** - Repository layer
- **H2 Database** - In-memory database
- **Spring Boot Starter Validation** - Bean validation (`@NotBlank`, `@Size`)

### Step 2: Configure Application Properties

`application.properties`:
- Set H2 as the datasource (`jdbc:h2:mem:taskdb`)
- Enable the H2 console for debugging (`/h2-console`)
- Configure JPA to auto-create tables (`spring.jpa.hibernate.ddl-auto=create-drop`)
- Set server port (8080)

### Step 3: Create the Data Model

- **`TaskStatus.java`** - Enum with values: `TODO`, `IN_PROGRESS`, `DONE`
- **`Task.java`** - JPA entity with:
  - `@Id @GeneratedValue` for the `id` field
  - `@NotBlank @Size(max = 100)` on `title`
  - `@Size(max = 500)` on `description`
  - `@Enumerated(EnumType.STRING)` on `status`
  - `dueDate` as `LocalDate`

### Step 4: Create the Repository

- **`TaskRepository.java`** - Interface extending `JpaRepository<Task, Long>`
- No custom queries needed for basic CRUD

### Step 5: Create the Service Layer

- **`TaskService.java`** - Business logic:
  - `getAllTasks()` - return all tasks
  - `getTaskById(Long id)` - return task or throw `TaskNotFoundException`
  - `createTask(Task task)` - set default status to `TODO` if not provided, save and return
  - `updateTask(Long id, Task task)` - find existing, update fields, save and return
  - `deleteTask(Long id)` - find existing, delete, or throw `TaskNotFoundException`

### Step 6: Create the REST Controller

- **`TaskController.java`** - `@RestController` with `@RequestMapping("/api/tasks")`:
  - Use `@Valid` on request bodies to trigger bean validation
  - Return appropriate HTTP status codes (`201` for create, `204` for delete)

### Step 7: Error Handling

- **`TaskNotFoundException.java`** - Custom runtime exception
- **`GlobalExceptionHandler.java`** - `@RestControllerAdvice`:
  - Handle `TaskNotFoundException` -> `404 Not Found`
  - Handle `MethodArgumentNotValidException` -> `400 Bad Request` with field error map

### Step 8: CORS Configuration

- **`CorsConfig.java`** - `@Configuration` with a `WebMvcConfigurer` bean:
  - Allow origin `http://localhost:5173` (Vite dev server)
  - Allow methods: GET, POST, PUT, DELETE
  - Allow all headers

---

## Frontend Implementation

### Step 1: Initialize React Project

Create a Vite React TypeScript project:
```bash
npm create vite@latest frontend -- --template react-ts
```

Install dependencies:
```bash
npm install axios
npm install -D tailwindcss @tailwindcss/vite
```

### Step 2: Define Types

**`types/Task.ts`**:
```typescript
export type TaskStatus = "TODO" | "IN_PROGRESS" | "DONE";

export interface Task {
  id?: number;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate?: string;
}
```

### Step 3: Create API Service

**`services/taskApi.ts`** - Axios-based service with functions:
- `getTasks()` - GET `/api/tasks`
- `getTask(id)` - GET `/api/tasks/{id}`
- `createTask(task)` - POST `/api/tasks`
- `updateTask(id, task)` - PUT `/api/tasks/{id}`
- `deleteTask(id)` - DELETE `/api/tasks/{id}`

Configure Axios base URL to `http://localhost:8080`.

### Step 4: Build Components

**`TaskList.tsx`** - Main component:
- Fetch and display all tasks on mount
- Show each task using `TaskItem`
- Include an "Add Task" button that opens `TaskForm`
- Optional: sort by status or due date

**`TaskForm.tsx`** - Create/Edit form:
- Fields: title (text input), description (textarea), status (dropdown), dueDate (date picker)
- Client-side validation: title required, title max 100 chars, description max 500 chars
- Display server-side validation errors from API responses
- On submit: call `createTask` or `updateTask` depending on mode

**`TaskItem.tsx`** - Single task display:
- Show title, description, status badge, due date
- Status dropdown for quick status changes
- Edit and Delete buttons
- Confirm before delete

### Step 5: Wire Up App.tsx

- Render `TaskList` as the main view
- Manage modal/inline form state for creating/editing tasks
- Display error messages on API failures (toast or inline)

### Step 6: Styling

- Use TailwindCSS utility classes for layout and styling
- Status badges with color coding (TODO: gray, IN_PROGRESS: blue, DONE: green)
- Responsive layout

---

## Testing Strategy

### Backend Tests

- **`TaskControllerTest.java`** - Integration tests using `@WebMvcTest`:
  - Test each endpoint (GET all, GET by ID, POST, PUT, DELETE)
  - Test validation errors (blank title, oversized fields)
  - Test 404 responses for missing tasks
- **`TaskServiceTest.java`** - Unit tests with Mockito:
  - Test business logic in isolation
  - Test exception handling for missing tasks

### Frontend Tests

- Component tests using React Testing Library (optional for MVP):
  - Test form validation
  - Test task list rendering
  - Test API error display

---

## How to Run

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api/tasks`.
The H2 console will be available at `http://localhost:8080/h2-console`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI will be available at `http://localhost:5173`.

---

## Success Criteria Checklist

- [ ] All CRUD operations work end-to-end (create, read, update, delete tasks)
- [ ] Form validation works (required title, character limits)
- [ ] API validation errors are displayed to the user
- [ ] Status can be changed via dropdown
- [ ] Tasks persist in H2 during the application session
- [ ] CORS is properly configured for frontend-backend communication
- [ ] Code is well-structured and understandable
- [ ] Backend tests pass
