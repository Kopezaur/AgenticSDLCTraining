# AgenticSDLCTraining

This repository contains the solution for the first task of the AI Core Heroes training.

It is a full-stack **Task Manager** application with CRUD operations, built with a Spring Boot backend and a React frontend.

## Prerequisites

- Java 11 and Maven
- Node.js (v16+) and npm

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`. An in-memory H2 database is used, so no external database setup is needed.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

Open `http://localhost:5173` in your browser to use the application.
