# taskflow-api

RESTful API for internal task and subtask management.

This project is being built with Java 17, Spring Boot and PostgreSQL, following a layered architecture and good practices for maintainability, validation, database migrations and automated testing.

## Current status

Initial project setup with:

- Java 17
- Spring Boot
- Maven Wrapper
- PostgreSQL with Docker Compose
- H2 in-memory database for automated tests
- PostgreSQL integration tests with Testcontainers
- Environment-based database configuration
- Flyway database migrations
- Domain entities:
    - AppUser
    - Task
    - Subtask
- TaskStatus enum
- UUID-based identifiers
- JPA/Hibernate mappings
- Database constraints for required fields, unique email and foreign keys
- Bean Validation for request payloads
- Consistent API validation messages for request payloads
- Standardized global API error handling
- Consistent error response structure
- Field-level validation error responses
- Safe 500 responses without stack trace exposure
- Preserved Spring MVC framework error statuses before fallback 500 handling
- Internal logging for unexpected server errors
- AppUser feature:
  - Create user
  - Find user by ID
  - Prevent duplicated email creation
  - Return 404 when user does not exist
- Task feature:
  - Create task
  - List tasks
  - Filter tasks by status and user
  - Paginate task results
  - Update task status
  - Use PENDING as the default status when none is provided
  - Set completedAt when a task is completed
  - Clear completedAt when a completed task is reopened
  - Prevent task completion while subtasks are unfinished
- Subtask feature:
  - Create subtask for a task
  - List subtasks from a task
  - Paginate subtask results
  - Update subtask status
  - Use PENDING as the default status when none is provided
  - Set completedAt when a subtask is completed
  - Clear completedAt when a completed subtask is reopened
  - Prevent completed tasks from having unfinished subtasks
- Automated tests for:
  - Domain validation
  - Request DTO validation
  - Persistence
  - Service business rules
  - Controller behavior with MockMvc

## API language

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

Before running the automated tests, make sure you have installed:

- Java 17+

The project includes Maven Wrapper scripts (`mvnw` and `mvnw.cmd`), so Maven does not need to be installed when using the documented commands. If you prefer to run `mvn` directly, install Maven separately.

To run the application locally with PostgreSQL or the PostgreSQL integration test profile, you also need:

- Docker
- Docker Compose

------------------------

## Environment configuration

This project does not commit real database credentials.

Create a local `.env` file based on `.env.example`:

```bash
cp .env.example .env
```

Then update the values if necessary:

```
POSTGRES_DB=taskflow_db
POSTGRES_USER=taskflow_user
POSTGRES_PASSWORD=change_me
```

------------------------

## Running PostgreSQL with Docker

Start the local PostgreSQL database:

```bash
docker compose up -d
```

Check if the container is running:

```bash
docker ps
```

To stop the database:

```bash
docker compose down
```

To stop the database and remove the volume:

```bash
docker compose down -v
```

Use docker compose down -v when you want to recreate the database from scratch and re-run the Flyway migrations.

------------------------

## Running the application

With PostgreSQL running, start the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
./mvnw.cmd spring-boot:run
```

When the application starts, Flyway will automatically apply the database migrations.

------------------------

## Running tests

Run the default automated test suite:

```bash
./mvnw test
```

On Windows:

```bash
./mvnw.cmd test
```

The default test suite uses an H2 in-memory database in PostgreSQL compatibility mode, so Docker is not required for the regular test workflow.

Spring integration tests that need a database activate the `test` profile with `@ActiveProfiles("test")`. This loads `src/test/resources/application-test.yaml` automatically during those tests, so no extra command-line profile is required for the default test suite.

To also run the PostgreSQL integration tests with Testcontainers, keep Docker running and use:

```bash
./mvnw test -Ppostgres-it
```

On Windows:

```bash
./mvnw.cmd test -Ppostgres-it
```

PostgreSQL-specific tests are tagged with `postgres` and validate the real database dialect, Flyway migrations, JPA mappings and database constraints against a disposable PostgreSQL container.

The current test suite validates:

* Domain object creation
* Required field validation
* Email format validation
* Entity persistence
* UUID generation
* Database constraints
* Foreign key constraints
* Unique email constraint
* User creation service rule
* Duplicated email prevention
* User search by existing ID
* NotFoundException when user does not exist
* HTTP 201 Created when creating a valid user
* HTTP 400 Bad Request for invalid payload
* HTTP 400 Bad Request for malformed JSON
* HTTP 400 Bad Request for invalid UUID path variables
* HTTP 409 Conflict for duplicated email
* HTTP 404 Not Found when user does not exist
* HTTP 405 Method Not Allowed for unsupported HTTP methods
* HTTP 415 Unsupported Media Type for unsupported request content types
* Standardized API error response structure
* Field-level validation errors
* Global exception handling with RestControllerAdvice
* HTTP 422 Unprocessable Content for business rule violations
* HTTP 500 Internal Server Error with generic message
* No stack trace exposure in API error responses
* Regression coverage to ensure Spring MVC client errors are not converted to 500
* Preservation of framework HTTP statuses in the global exception handler
* Task creation and listing
* Task filtering by status and user
* Task status update through PATCH /tasks/{id}/status
* UpdateTaskStatusRequest validation
* Task status transitions in the domain model
* TaskService status update business rules
* CreateAppUserRequest validation
* CreateTaskRequest validation
* Subtask creation and listing
* Subtask pagination
* Subtask status update through PATCH /subtasks/{id}/status
* CreateSubtaskRequest validation
* UpdateSubtaskStatusRequest validation
* Prevention of unfinished subtasks on completed tasks
* PostgreSQL integration behavior through the optional `postgres-it` profile

------------------------

## Error response format

The API uses a standardized error response for validation errors, Spring MVC framework errors, business errors and unexpected server errors.

Example:

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

Error response fields:

```
timestamp - date and time when the error occurred
status - HTTP status code
error - HTTP reason phrase
message - safe error message
path - request path
fields - field-level validation errors, when applicable
```

The API does not expose stack traces, exception class names or internal implementation details in JSON error responses.

API request validation messages follow a consistent English style: sentence case, clear field name, and a final period.

------------------------

### Error status codes

| Status | Meaning |
|--------|---------|
| 400 Bad Request | Invalid request payload, malformed JSON, invalid path variable or Bean Validation error |
| 404 Not Found | Requested resource was not found |
| 405 Method Not Allowed | HTTP method is not supported for the endpoint |
| 409 Conflict | Business conflict or unique constraint violation |
| 415 Unsupported Media Type | Request content type is not supported |
| 422 Unprocessable Content | Business rule violation |
| 500 Internal Server Error | Unexpected error with generic safe message |

------------------------

## Error logging

Unexpected server errors are logged internally with stack trace for diagnostics.

The API response remains safe and does not expose stack traces, exception class names or internal implementation details to clients.

By default, Spring Boot writes logs to the application console/stdout. When running with Docker, logs can be inspected with:

```bash
docker compose logs -f
```

------------------------

## Database migrations

Database schema changes are managed by Flyway.

Current migrations:

```bash
src/main/resources/db/migration/V1__create_initial_schema.sql
src/main/resources/db/migration/V2__add_task_filter_indexes.sql
```

The first migration creates the following tables:

- app_users
- tasks
- subtasks

With constraints for:

- Primary keys using UUID
- Required fields using NOT NULL
- Unique user email
- Foreign key from tasks.user_id to app_users.id
- Foreign key from subtasks.task_id to tasks.id
- Valid task status values

The second migration adds indexes to improve task filtering by user and status.

------------------------

## API endpoints

<h3>Users</h3>

Create user

``` HTTP
POST /users
```

Request body:

```
{
  "name": "John Doe",
  "email": "john.doe@email.com"
}
```

Successful response:

``` HTTP
201 Created
```

JSON:

```
{
  "id": "b1ef8ab6-4be9-4487-8744-e1bedc43988c",
  "name": "John Doe",
  "email": "john.doe@email.com"
}
```

Validation error response:

``` HTTP
400 Bad Request
```

```
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

Duplicated email response:

``` HTTP
409 Conflict
```

```
{
"timestamp": "2026-05-17T19:30:00",
"status": 409,
"error": "Conflict",
"message": "Unable to create user with provided data.",
"path": "/users",
"fields": []
}
```

<h3>Find user by ID</h3>

``` HTTP
GET /users/{id}
```

Successful response:

``` HTTP
200 OK
```

JSON:

``` JSON
{
  "id": "b1ef8ab6-4be9-4487-8744-e1bedc43988c",
  "name": "John Doe",
  "email": "john.doe@email.com"
}
```

User not found:

```
404 Not Found
```

Invalid UUID:

``` HTTP
400 Bad Request
```

<h3>Tasks</h3>

Create task

``` HTTP
POST /tasks
```

Request body:

``` JSON
{
  "title": "Implement task creation",
  "description": "Create POST /tasks endpoint",
  "userId": "b1ef8ab6-4be9-4487-8744-e1bedc43988c",
  "status": "IN_PROGRESS"
}
```

The `status` field is optional. When omitted, the task is created with `PENDING`.

Successful response:

``` HTTP
201 Created
Location: /tasks/{id}
```

JSON:

``` JSON
{
  "id": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42",
  "title": "Implement task creation",
  "description": "Create POST /tasks endpoint",
  "status": "IN_PROGRESS",
  "createdAt": "2026-05-18T10:30:00Z",
  "completedAt": null,
  "userId": "b1ef8ab6-4be9-4487-8744-e1bedc43988c"
}
```

Validation error response:

``` HTTP
400 Bad Request
```

User not found:

``` HTTP
404 Not Found
```

<h3>List tasks</h3>

``` HTTP
GET /tasks
```

Optional query parameters:

```
status - PENDING, IN_PROGRESS or COMPLETED
userId - user UUID
page - page number, starting at 0
size - page size
sort - sort expression
```

By default, results use page size 20 and are sorted by `createdAt` descending. The maximum accepted page size is 100.

Example:

``` HTTP
GET /tasks?status=PENDING&userId=b1ef8ab6-4be9-4487-8744-e1bedc43988c&page=0&size=20
```

Successful response:

``` HTTP
200 OK
```

JSON:

``` JSON
{
  "content": [
    {
      "id": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42",
      "title": "Implement task filters",
      "description": null,
      "status": "PENDING",
      "createdAt": "2026-05-18T10:30:00Z",
      "completedAt": null,
      "userId": "b1ef8ab6-4be9-4487-8744-e1bedc43988c"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Invalid status filter:

``` HTTP
400 Bad Request
```

<h3>Update task status</h3>

``` HTTP
PATCH /tasks/{id}/status
```

Request body:

``` JSON
{
  "status": "COMPLETED"
}
```

Successful response:

``` HTTP
200 OK
```

JSON:

``` JSON
{
  "id": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42",
  "title": "Implement task creation",
  "description": "Create POST /tasks endpoint",
  "status": "COMPLETED",
  "createdAt": "2026-05-18T10:30:00Z",
  "completedAt": "2026-05-18T11:00:00Z",
  "userId": "b1ef8ab6-4be9-4487-8744-e1bedc43988c"
}
```

Validation error response:

``` HTTP
400 Bad Request
```

Task not found:

``` HTTP
404 Not Found
```

Unfinished subtasks response:

``` HTTP
422 Unprocessable Content
```

JSON:

``` JSON
{
  "timestamp": "2026-05-18T11:05:00",
  "status": 422,
  "error": "Unprocessable Content",
  "message": "Task cannot be completed because it has unfinished subtasks.",
  "path": "/tasks/cc7c20b3-723e-4b82-8491-3f95fd3eaf42/status",
  "fields": []
}
```

<h3>Subtasks</h3>

Create subtask

``` HTTP
POST /tasks/{taskId}/subtasks
```

Request body:

``` JSON
{
  "title": "Write controller tests",
  "description": "Cover subtask endpoints with MockMvc",
  "status": "PENDING"
}
```

The `status` field is optional. When omitted, the subtask is created with `PENDING`.

Successful response:

``` HTTP
201 Created
```

JSON:

``` JSON
{
  "id": "5a86ed15-95b7-47f4-99c4-5461d653efde",
  "title": "Write controller tests",
  "description": "Cover subtask endpoints with MockMvc",
  "status": "PENDING",
  "createdAt": "2026-05-18T10:45:00Z",
  "completedAt": null,
  "taskId": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42"
}
```

Validation error response:

``` HTTP
400 Bad Request
```

Task not found:

``` HTTP
404 Not Found
```

Completed task with unfinished subtask response:

``` HTTP
422 Unprocessable Content
```

JSON:

``` JSON
{
  "timestamp": "2026-05-18T11:05:00",
  "status": 422,
  "error": "Unprocessable Content",
  "message": "Cannot create unfinished subtask for a completed task.",
  "path": "/tasks/cc7c20b3-723e-4b82-8491-3f95fd3eaf42/subtasks",
  "fields": []
}
```

<h3>List subtasks from a task</h3>

``` HTTP
GET /tasks/{taskId}/subtasks
```

Optional query parameters:

```
page - page number, starting at 0
size - page size
sort - sort expression
```

By default, results use page size 20 and are sorted by `createdAt` ascending. The maximum accepted page size is 100.

Example:

``` HTTP
GET /tasks/cc7c20b3-723e-4b82-8491-3f95fd3eaf42/subtasks?page=0&size=20
```

Successful response:

``` HTTP
200 OK
```

JSON:

``` JSON
{
  "content": [
    {
      "id": "5a86ed15-95b7-47f4-99c4-5461d653efde",
      "title": "Write controller tests",
      "description": "Cover subtask endpoints with MockMvc",
      "status": "PENDING",
      "createdAt": "2026-05-18T10:45:00Z",
      "completedAt": null,
      "taskId": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Task not found:

``` HTTP
404 Not Found
```

<h3>Update subtask status</h3>

``` HTTP
PATCH /subtasks/{id}/status
```

Request body:

``` JSON
{
  "status": "COMPLETED"
}
```

Successful response:

``` HTTP
200 OK
```

JSON:

``` JSON
{
  "id": "5a86ed15-95b7-47f4-99c4-5461d653efde",
  "title": "Write controller tests",
  "description": "Cover subtask endpoints with MockMvc",
  "status": "COMPLETED",
  "createdAt": "2026-05-18T10:45:00Z",
  "completedAt": "2026-05-18T11:00:00Z",
  "taskId": "cc7c20b3-723e-4b82-8491-3f95fd3eaf42"
}
```

Validation error response:

``` HTTP
400 Bad Request
```

Subtask not found:

``` HTTP
404 Not Found
```

------------------------

## Domain model

<h3>AppUser</h3>

Represents an application user.

Fields:

```
- id
- name
- email
```

Rules:

- name is required
- email is required
- email must be unique
- email must have a valid format

<h3>Task</h3>

Represents a user task.

Fields:

```
id
title
description
status
createdAt
completedAt
user
```

Rules:

- title is required
- status is required
- status defaults to PENDING when omitted during creation
- createdAt is required
- completedAt is set when status is COMPLETED
- completedAt is cleared when status changes back to PENDING or IN_PROGRESS
- user is required
- task cannot be completed while any subtask is still PENDING or IN_PROGRESS

This rule is enforced during task status updates. If any subtask is still `PENDING` or `IN_PROGRESS`, the API returns `422 Unprocessable Content` instead of completing the task.

<h3>Subtask</h3>

Represents a task subdivision.

Fields:

```
id
title
description
status
createdAt
completedAt
task
```

Rules:

- title is required
- status is required
- status defaults to PENDING when omitted during creation
- createdAt is required
- completedAt is set when status is COMPLETED
- completedAt is cleared when status changes back to PENDING or IN_PROGRESS
- task is required
- PENDING or IN_PROGRESS subtasks cannot be created for or applied to completed tasks

The last rule is an extra consistency safeguard. It prevents a completed task from later ending up with unfinished subtasks, either by creating a new unfinished subtask or by reopening an existing completed subtask.

<h3>TaskStatus</h3>

Available values:

```
PENDING
IN_PROGRESS
COMPLETED
```

------------------------

## Technologies

* Java 17
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* PostgreSQL
* H2
* Flyway
* Docker Compose
* JUnit 5
* Mockito
* AssertJ
* MockMvc
* Testcontainers
* Bean Validation
* JaCoCo

