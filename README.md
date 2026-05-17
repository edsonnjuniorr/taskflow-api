# taskflow-api

RESTful API for internal task and subtaskTest management.

This project is being built with Java 17, Spring Boot and PostgreSQL, following a layered architecture and good practices for maintainability, validation, database migrations and automated testing.

## Current status

Initial project setup with:

- Java 17
- Spring Boot
- Maven Wrapper
- PostgreSQL with Docker Compose
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
- Automated tests for domain validation and persistence

## Requirements

Before running the project, make sure you have installed:

- Java 17+
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

Run all automated tests:

```bash
./mvnw test
```

On Windows:

```bash
./mvnw.cmd test
```

The current test suite validates:

Domain object creation
Required field validation
Email format validation
Entity persistence
UUID generation
Database constraints
Foreign key constraints
Unique email constraint

Some persistence tests use Testcontainers with PostgreSQL to validate the real database behavior.

------------------------

## Database migrations

Database schema changes are managed by Flyway.

Current migration:

```bash
src/main/resources/db/migration/V1__create_initial_schema.sql
```

This migration creates the following tables:

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
- createdAt is required
- user is required

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
- createdAt is required
- task is required

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
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Docker Compose
* JUnit 5
* AssertJ
* Testcontainers
* Bean Validation

