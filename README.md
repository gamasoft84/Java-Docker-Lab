# JavaDockerLab

A hands-on microservices lab built with **Java 17 + Spring Boot 3**, **Spring Cloud Gateway**,
**PostgreSQL** (one database per service) and **Docker Compose**. It includes input
validation, a consistent JSON error format and a single entry point through an API Gateway.

> Learning-focused project for a portfolio. No JWT, Kafka or Kubernetes yet (see [Next Steps](#next-steps)).

## Project Overview

- **4 services**: an API Gateway plus 3 business microservices (usuarios, productos, pedidos).
- **Database per service**: each microservice owns its own PostgreSQL instance.
- **Service-to-service communication**: `pedidos-service` calls `usuarios-service` and
  `productos-service` over HTTP to validate references before creating an order.
- **Single entry point**: every request goes through the gateway at `http://localhost:8080`.
- **Production-style basics**: Jakarta Validation on DTOs, custom exceptions, a global
  exception handler returning a uniform JSON error body, healthchecks and named volumes.

## Architecture

```
                         client / Postman / curl
                                   │
                                   ▼
                        ┌────────────────────┐
                        │     api-gateway     │ :8080   (Spring Cloud Gateway)
                        └─────────┬──────────-┘
            ┌──────────────────---┼──────────────────────┐
   /api/usuarios/**       /api/productos/**         /api/pedidos/**
            ▼                     ▼                       ▼
 ┌──────────────────┐  ┌──────────────────┐    ┌──────────────────┐
 │ usuarios-service │  │ productos-service│    │  pedidos-service │
 │      :8081       │  │      :8082       │    │      :8083       │
 └────────┬─────────┘  └─────────┬────────┘    └────┬────────┬────┘
          ▼                      ▼                   │        ▼
     usuarios-db            productos-db             │   pedidos-db
     (PostgreSQL)           (PostgreSQL)             │   (PostgreSQL)
                                                     │
            validates over HTTP (usuarios + productos) ◄┘
```

Each microservice follows the same layout:

```
service/
├── Dockerfile                 # multi-stage build (compiles with Maven inside the container)
├── pom.xml
└── src/main/
    ├── java/com/lab/<service>/
    │   ├── controller/        # REST endpoints (@Valid)
    │   ├── service/           # business logic
    │   ├── repository/        # Spring Data JPA
    │   ├── model/             # @Entity
    │   ├── dto/               # request objects + validation annotations
    │   ├── exception/         # ErrorResponse, custom exceptions, GlobalExceptionHandler
    │   ├── config/            # sample data seeder (usuarios & productos only)
    │   └── client/            # (pedidos only) HTTP clients to other services
    └── resources/application.properties
```

## Services and Ports

| Service           | Port  | Description                                   | Database     |
|-------------------|-------|-----------------------------------------------|--------------|
| api-gateway       | 8080  | Single entry point (Spring Cloud Gateway)     | —            |
| usuarios-service  | 8081  | Users CRUD                                    | usuarios-db  |
| productos-service | 8082  | Products CRUD (with stock)                    | productos-db |
| pedidos-service   | 8083  | Orders; validates users/products over HTTP    | pedidos-db   |

## Databases

Each service has its own PostgreSQL database (*database per service*). They are also exposed
to the host for inspection with a SQL client:

| Database     | Host port | DB / User / Password   |
|--------------|-----------|------------------------|
| usuarios-db  | 5433      | `usuarios`             |
| productos-db | 5434      | `productos`            |
| pedidos-db   | 5435      | `pedidos`              |

- Schema is created/updated automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
- `usuarios-service` and `productos-service` seed sample data **only when their table is empty**.

## API Gateway Routes

The gateway strips the `/api` prefix (`StripPrefix=1`) and forwards to the target service.

| Gateway route        | Forwards to                                  |
|----------------------|----------------------------------------------|
| `/api/usuarios/**`   | `http://usuarios-service:8081/usuarios/**`   |
| `/api/productos/**`  | `http://productos-service:8082/productos/**` |
| `/api/pedidos/**`    | `http://pedidos-service:8083/pedidos/**`     |

Example: `GET /api/usuarios/1` reaches `usuarios-service` as `GET /usuarios/1`.

## Docker Commands

```bash
# Build images and start everything (gateway + 3 services + 3 databases)
docker compose up --build

# Same, in the background
docker compose up --build -d

# Stop and remove containers + network (DATA IS KEPT in volumes)
docker compose down

# Stop and ALSO delete volumes (fresh databases next time)
docker compose down -v

# Useful
docker compose ps                            # container status
docker compose logs -f api-gateway           # follow a service's logs
docker compose up -d --build usuarios-service  # rebuild only one service
```

## Manual Testing with curl

All requests go through the gateway at `http://localhost:8080`.
A Postman collection is also available at
[`docs/postman/JavaDockerLab.postman_collection.json`](docs/postman/JavaDockerLab.postman_collection.json)
(import it into Postman and use the `baseUrl` variable).

### Users

```bash
# List users (4 seeded by default)
curl http://localhost:8080/api/usuarios

# Get one
curl http://localhost:8080/api/usuarios/1

# Create user (201 Created)
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Nuevo Usuario", "email": "nuevo@example.com"}'
```

### Products

```bash
# List products (2 seeded by default)
curl http://localhost:8080/api/productos

# Create product (201 Created)
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Monitor 24", "precio": 129.99, "stock": 15}'
```

### Orders

```bash
# Create a valid order (201 Created)
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 2}'

# List orders
curl http://localhost:8080/api/pedidos
```

> Direct access (bypassing the gateway), handy for debugging:
> `http://localhost:8081/usuarios`, `http://localhost:8082/productos`, `http://localhost:8083/pedidos`.

## Validation Examples

DTOs are validated with Jakarta Validation (`@NotBlank`, `@NotNull`, `@Email`, `@Positive`,
`@PositiveOrZero`). Invalid input returns **400 Bad Request** with the error format below.

```bash
# Invalid email -> 400
curl -i -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre": "X", "email": "not-an-email"}'

# Price must be > 0 -> 400
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Bad", "precio": 0, "stock": 5}'

# Order for a non-existent user -> 404
curl -i -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 999, "productoId": 1, "cantidad": 1}'

# Order for a non-existent product -> 404
curl -i -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 999, "cantidad": 1}'

# Not enough stock (business rule) -> 400
curl -i -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 99999}'
```

### Validation rules

| Service   | Field      | Rule                          |
|-----------|------------|-------------------------------|
| usuarios  | nombre     | `@NotBlank`                   |
| usuarios  | email      | `@NotBlank` + `@Email`        |
| productos | nombre     | `@NotBlank`                   |
| productos | precio     | `@Positive` (> 0)             |
| productos | stock      | `@PositiveOrZero` (>= 0)      |
| pedidos   | usuarioId  | `@NotNull`                    |
| pedidos   | productoId | `@NotNull`                    |
| pedidos   | cantidad   | `@Positive` (> 0)             |

## Error Response Format

Every error (validation, not found, business rule, unexpected) returns the same JSON shape,
produced by a `@RestControllerAdvice` (`GlobalExceptionHandler`) in each service:

```json
{
  "timestamp": "2026-05-29T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "email: El email no tiene un formato valido",
  "path": "/usuarios"
}
```

HTTP status codes used:

| Status | When                                                        |
|--------|-------------------------------------------------------------|
| 200    | Successful GET                                              |
| 201    | Successful creation (POST)                                  |
| 400    | Invalid input (validation) or business rule (e.g. no stock) |
| 404    | Resource not found (`ResourceNotFoundException`)            |
| 500    | Unexpected, uncontrolled error                             |

Custom exceptions: `ResourceNotFoundException` (→ 404) and `BusinessException` (→ 400).

## Database Persistence

- Each database stores its files in a **named Docker volume**, so data **survives**
  `docker compose down` and restarts:
  - `usuarios-data`, `productos-data`, `pedidos-data`
- To wipe all data and start fresh, delete the volumes:

```bash
docker compose down -v
```

On the next `docker compose up --build`, tables are recreated empty and sample users/products
are seeded again.

## DBeaver Connection Info

With the containers running, create a PostgreSQL connection per database:

| Field    | usuarios-db | productos-db | pedidos-db |
|----------|-------------|--------------|------------|
| Host     | localhost   | localhost    | localhost  |
| Port     | 5433        | 5434         | 5435       |
| Database | usuarios    | productos    | pedidos    |
| Username | usuarios    | productos    | pedidos    |
| Password | usuarios    | productos    | pedidos    |

Or open a shell directly inside a container:

```bash
docker exec -it usuarios-db psql -U usuarios -d usuarios
```

## Requirements

Only **Docker** is required (Docker Desktop or Colima). Java and Maven are **not** needed:
each `Dockerfile` compiles its service inside the container (multi-stage build).

## Next Steps

- **JWT Authentication** — secure the gateway and propagate identity to services.
- **Centralized logging** — aggregate logs (e.g. ELK / Loki) across services.
- **Kafka events** — emit/consume domain events (e.g. order created) asynchronously.
- **Kubernetes deployment** — manifests/Helm to run the stack on a cluster.
