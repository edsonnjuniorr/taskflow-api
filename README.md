# taskflow-api

RESTful API for internal task and subtask management.

The project was built with Java 17, Spring Boot, PostgreSQL, Flyway, Docker and automated tests, following a layered architecture where API controllers depend on service contracts, service implementations handle business workflows, domain objects enforce core rules and API errors are standardized.

## Features

- Create and find users
- Validate unique and well-formed user emails
- Create, filter and update tasks
- Create, list and update subtasks
- Enforce task completion only when all subtasks are completed
- Manage `completedAt` automatically based on status
- Paginate task and subtask listings
- Validate request payloads with Bean Validation
- Handle API errors with a consistent JSON response
- Document the API with Swagger/OpenAPI
- Run the application with Docker Compose
- Run automated tests locally or in GitHub Actions

## API Language

The challenge allows choosing the language used for entity and endpoint names.

This project uses English names consistently for endpoints, JSON fields and enum values:

| Challenge term | Project term |
|----------------|--------------|
| `/usuarios` | `/users` |
| `/tarefas` | `/tasks` |
| `/subtarefas` | `/subtasks` |
| `nome` | `name` |
| `titulo` | `title` |
| `descricao` | `description` |
| `dataCriacao` | `createdAt` |
| `dataConclusao` | `completedAt` |
| `usuarioId` | `userId` |
| `tarefaId` | `taskId` |
| `PENDENTE` | `PENDING` |
| `EM_ANDAMENTO` | `IN_PROGRESS` |
| `CONCLUIDA` | `COMPLETED` |

## Requirements

To run the project with Docker:

- Docker
- Docker Compose

To run the project or tests locally without Docker:

- Java 17+

The project includes Maven Wrapper scripts (`mvnw` and `mvnw.cmd`), so Maven does not need to be installed when using the documented commands.

## Environment

The project does not commit real database credentials.

Docker Compose has safe local defaults, so the application can run without a `.env` file. To customize ports or database values, create a local `.env` from `.env.example`:

```bash
cp .env.example .env
```

Available values:

```env
POSTGRES_DB=taskflow_db
POSTGRES_USER=taskflow_user
POSTGRES_PASSWORD=taskflow_password
APP_PORT=8080
POSTGRES_PORT=5432
```

## Running With Docker

Start the application and PostgreSQL:

```bash
docker compose up --build
```

Run in the background:

```bash
docker compose up --build -d
```

The API will be available at:

```text
http://localhost:8080
```

Swagger UI will be available at:

```text
http://localhost:8080/swagger-ui.html
```

Stop the containers:

```bash
docker compose down
```

To remove the PostgreSQL volume and recreate the database from scratch:

```bash
docker compose down -v
```

## Running Locally

If you prefer to run the application with Maven Wrapper, start only PostgreSQL first:

```bash
docker compose up -d postgres
```

Then start the application.

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The local application uses the default PostgreSQL connection values from `.env.example`.

If you customize database values, provide the matching environment variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

## API Documentation

With the application running:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
```

The OpenAPI version is generated from the Maven project version during the build.

## Running Tests

Run the default automated test suite.

On Linux/macOS:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

The default suite uses H2 in PostgreSQL compatibility mode, so Docker is not required.

To also run PostgreSQL integration tests with Testcontainers, keep Docker running.

On Linux/macOS:

```bash
./mvnw test -Ppostgres-it
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test -Ppostgres-it
```

Test coverage includes:

- DTO validation tests
- Domain model tests
- Service implementation business rule tests
- Controller tests with MockMvc
- Persistence tests with H2
- End-to-end API flow tests
- Optional PostgreSQL integration tests with Testcontainers

## CI

The repository includes a GitHub Actions workflow at `.github/workflows/ci.yml`.

It runs the Maven test suite on:

- pushes to `main` or `master`
- pull requests

## API Endpoints

### Users

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/users` | Create a user |
| `GET` | `/users/{id}` | Find a user by ID |

Create user request:

```json
{
  "name": "John Doe",
  "email": "john.doe@email.com"
}
```

### Tasks

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/tasks` | Create a task for a user |
| `GET` | `/tasks` | List tasks with optional filters |
| `PATCH` | `/tasks/{id}/status` | Update task status |

Create task request:

```json
{
  "title": "Implement task creation",
  "description": "Create POST /tasks endpoint",
  "userId": "b1ef8ab6-4be9-4487-8744-e1bedc43988c",
  "status": "IN_PROGRESS"
}
```

List tasks:

```http
GET /tasks?status=PENDING&userId=b1ef8ab6-4be9-4487-8744-e1bedc43988c&page=0&size=20&sort=createdAt,desc
```

Supported query parameters:

| Parameter | Description |
|-----------|-------------|
| `status` | `PENDING`, `IN_PROGRESS` or `COMPLETED` |
| `userId` | User UUID |
| `page` | Page number, starting at `0` |
| `size` | Page size, maximum `100` |
| `sort` | Sort expression in the format `property,direction`, for example `sort=createdAt,desc`. Supported properties: `id`, `title`, `description`, `status`, `createdAt`, `completedAt` |

Update task status request:

```json
{
  "status": "COMPLETED"
}
```

### Subtasks

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/tasks/{taskId}/subtasks` | Create a subtask for a task |
| `GET` | `/tasks/{taskId}/subtasks` | List subtasks from a task |
| `PATCH` | `/subtasks/{id}/status` | Update subtask status |

Create subtask request:

```json
{
  "title": "Write controller tests",
  "description": "Cover subtask endpoints with MockMvc",
  "status": "PENDING"
}
```

List subtasks:

```http
GET /tasks/cc7c20b3-723e-4b82-8491-3f95fd3eaf42/subtasks?page=0&size=20&sort=createdAt,asc
```

Update subtask status request:

```json
{
  "status": "COMPLETED"
}
```

## Business Rules

### User

- `name` is required
- `email` is required, unique and must have a valid format

### Task

- `title` is required
- `status` is required
- `createdAt` is generated automatically
- `completedAt` is set only when status is `COMPLETED`
- `completedAt` is cleared when status changes back to `PENDING` or `IN_PROGRESS`
- a task can only be completed when all subtasks are `COMPLETED`

### Subtask

- `title` is required
- `status` is required
- `createdAt` is generated automatically
- `completedAt` is set only when status is `COMPLETED`
- `completedAt` is cleared when status changes back to `PENDING` or `IN_PROGRESS`
- updating a subtask does not automatically update its parent task
- unfinished subtasks prevent task completion
- completed tasks cannot receive or reopen unfinished subtasks

### TaskStatus

```text
PENDING
IN_PROGRESS
COMPLETED
```

## Error Responses

The API uses a consistent error response format:

```json
{
  "timestamp": "2026-05-17T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/users",
  "fields": [
    {
      "field": "email",
      "message": "Email must be valid."
    }
  ]
}
```

Common status codes:

| Status | Meaning |
|--------|---------|
| `400` | Invalid request, malformed JSON, invalid path variable or validation error |
| `404` | Resource not found |
| `405` | HTTP method not supported |
| `409` | Conflict or unique constraint violation |
| `415` | Unsupported content type |
| `422` | Business rule violation |
| `500` | Unexpected server error with safe generic message |

The API does not expose stack traces, exception class names or internal implementation details in JSON responses.

## Database Migrations

Flyway manages schema migrations under:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__create_initial_schema.sql
V2__add_task_filter_indexes.sql
```

## Technologies

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Bean Validation
- PostgreSQL
- H2
- Flyway
- springdoc-openapi
- Docker
- Docker Compose
- JUnit 5
- Mockito
- AssertJ
- MockMvc
- Testcontainers
- JaCoCo
- GitHub Actions
