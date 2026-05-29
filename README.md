# JavaDockerLab — Microservicios con Spring Boot + Docker + PostgreSQL + API Gateway

Proyecto de práctica para entender microservicios, Docker y persistencia con base de datos.
Cada microservicio tiene **su propia base de datos PostgreSQL** (patrón *database per service*),
usa **Spring Data JPA**, y todo se accede a través de un **API Gateway** (Spring Cloud Gateway).

## Arquitectura

```
                         cliente / Postman / curl
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
            valida por HTTP a usuarios y productos ◄─┘
```

- **api-gateway** (`:8080`): único punto de entrada. Enruta `/api/...` hacia cada servicio.
- **usuarios-service** (`:8081`) → `usuarios-db`
- **productos-service** (`:8082`) → `productos-db`
- **pedidos-service** (`:8083`) → `pedidos-db`. Antes de guardar un pedido, valida por HTTP
  que el usuario y el producto existan, y que haya stock suficiente.

### Rutas del gateway

| Ruta en el gateway        | Se enruta a                              |
|---------------------------|------------------------------------------|
| `/api/usuarios/**`        | `http://usuarios-service:8081/usuarios/**`  |
| `/api/productos/**`       | `http://productos-service:8082/productos/**`|
| `/api/pedidos/**`         | `http://pedidos-service:8083/pedidos/**`    |

> El gateway usa el filtro `StripPrefix=1`, que elimina el primer segmento (`/api`)
> antes de reenviar. Así `/api/usuarios/1` llega al servicio como `/usuarios/1`.
> Los puertos 8081/8082/8083 siguen disponibles directamente si quieres saltarte el gateway.

Cada servicio tiene esta estructura:

```
servicio/
├── Dockerfile                 # build multi-stage (compila con Maven dentro del contenedor)
├── pom.xml
└── src/main/
    ├── java/com/lab/<servicio>/
    │   ├── controller/        # endpoints REST
    │   ├── service/           # lógica de negocio
    │   ├── repository/        # Spring Data JPA (JpaRepository)
    │   ├── model/             # entidades @Entity
    │   ├── dto/               # objetos de request
    │   ├── config/            # carga datos de ejemplo (solo usuarios y productos)
    │   └── client/            # (solo pedidos) llamadas HTTP a otros servicios
    └── resources/application.properties
```

## Requisitos

Solo necesitas **Docker** (Docker Desktop o Colima). No hace falta Java ni Maven:
cada `Dockerfile` compila el servicio dentro del contenedor (build multi-stage).

## Persistencia y base de datos

- Cada base de datos guarda sus archivos en un **volumen** de Docker, así los datos
  **sobreviven** a `docker compose down` y reinicios:
  - `usuarios-data`, `productos-data`, `pedidos-data`
- Hibernate crea/actualiza las tablas automáticamente al arrancar
  (`spring.jpa.hibernate.ddl-auto=update`).
- `usuarios-service` y `productos-service` cargan datos de ejemplo **solo si la tabla está vacía**.
- Las bases también quedan expuestas a tu máquina por si quieres conectarte con un cliente SQL:
  - `usuarios-db` → `localhost:5433`
  - `productos-db` → `localhost:5434`
  - `pedidos-db`   → `localhost:5435`
  - (usuario/contraseña/base coinciden con el nombre: `usuarios`/`usuarios`/`usuarios`, etc.)

## 1. Levantar todo

Desde la raíz del proyecto (`JavaDockerLab/`):

```bash
docker compose up --build
```

Esto construye las imágenes, arranca las 3 bases de datos, espera a que estén **listas**
(healthcheck) y luego levanta los 3 microservicios. En segundo plano: agrega `-d`.

La primera vez tarda un poco porque descarga dependencias de Maven e imágenes de Postgres.

> **Todo el acceso es a través del gateway en `http://localhost:8080`.**
> Solo cambia el prefijo: usa `/api/usuarios`, `/api/productos`, `/api/pedidos`.

## 2. Probar usuarios — vía gateway (http://localhost:8080)

```bash
# Listar usuarios (vienen 4 de ejemplo)
curl http://localhost:8080/api/usuarios

# Obtener uno
curl http://localhost:8080/api/usuarios/1

# Crear usuario
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Nuevo Usuario", "email": "nuevo@example.com"}'
```

## 3. Probar productos — vía gateway

```bash
# Listar productos (vienen 2 de ejemplo)
curl http://localhost:8080/api/productos

# Obtener uno
curl http://localhost:8080/api/productos/1

# Crear producto
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Monitor 24", "precio": 129.99, "stock": 15}'
```

## 4. Crear pedidos — vía gateway

```bash
# Crear pedido válido (usuario 1 compra 2 unidades del producto 1)
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 2}'

# Listar pedidos
curl http://localhost:8080/api/pedidos

# Validación: usuario inexistente -> 400 Bad Request
curl -i -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 999, "productoId": 1, "cantidad": 1}'

# Validación de stock -> 400 Bad Request
curl -i -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 99999}'
```

En **Postman**: para los `POST` usa Body → raw → JSON con el cuerpo indicado.
Un pedido válido devuelve `201 Created` con `nombreUsuario`, `nombreProducto` y `total`.

## 5. Endpoints disponibles

Todos a través del gateway (`http://localhost:8080`):

| Recurso   | Método | Ruta (gateway)         |
|-----------|--------|------------------------|
| usuarios  | GET    | `/api/usuarios`        |
| usuarios  | GET    | `/api/usuarios/{id}`   |
| usuarios  | POST   | `/api/usuarios`        |
| productos | GET    | `/api/productos`       |
| productos | GET    | `/api/productos/{id}`  |
| productos | POST   | `/api/productos`       |
| pedidos   | GET    | `/api/pedidos`         |
| pedidos   | GET    | `/api/pedidos/{id}`    |
| pedidos   | POST   | `/api/pedidos`         |

> Acceso directo (sin gateway), útil para depurar: `http://localhost:8081/usuarios`,
> `http://localhost:8082/productos`, `http://localhost:8083/pedidos`.

## 6. Detener los contenedores

```bash
# Si está en primer plano: Ctrl + C

# Detener y eliminar contenedores y red (los datos SE CONSERVAN en los volúmenes)
docker compose down
```

## 7. Reiniciar la base de datos desde cero (borrar volúmenes)

```bash
# Detiene todo y BORRA los volúmenes -> se pierden todos los datos
docker compose down -v
```

Al volver a `docker compose up --build`, las tablas se recrean vacías y se vuelven
a cargar los datos de ejemplo de usuarios y productos.

## Comandos útiles

```bash
docker compose ps                          # ver estado de contenedores
docker compose logs -f pedidos-service     # seguir logs de un servicio
docker compose up -d --build usuarios-service   # reconstruir solo un servicio

# Entrar a una base con psql
docker exec -it usuarios-db psql -U usuarios -d usuarios
```

## Notas
- El **api-gateway** (Spring Cloud Gateway) es el único punto de entrada público (`:8080`)
  y enruta por nombre de servicio dentro de la red (`http://usuarios-service:8081`, etc.).
- `pedidos-service` descubre a los otros servicios por su **nombre de servicio** dentro
  de la red de Docker (`http://usuarios-service:8081`). Para hablar con su base usa
  `jdbc:postgresql://pedidos-db:5432/pedidos`. Dentro de Docker nunca se usa `localhost`.
- Cada servicio tiene su propia base: no comparten tablas (database per service).
- No incluye aún: seguridad JWT, Kubernetes ni Kafka (siguientes pasos).
