# taskflow-api

RESTful API for internal task and subtask management.

This project is being built with Java 17, Spring Boot and PostgreSQL, following a layered architecture and good practices for maintainability, validation and automated testing.

## Current status

Initial project setup with:

- Java 17
- Spring Boot
- Maven Wrapper
- PostgreSQL with Docker Compose
- Environment-based database configuration

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