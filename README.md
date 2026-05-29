# JavaDockerLab — 3 microservicios con Spring Boot + Docker + PostgreSQL

Proyecto de práctica para entender microservicios, Docker y persistencia con base de datos.
Cada microservicio tiene **su propia base de datos PostgreSQL** (patrón *database per service*)
y usa **Spring Data JPA**.

## Arquitectura

```
                 ┌──────────────────┐
   crea pedido   │  pedidos-service │ :8083 ──► pedidos-db   (PostgreSQL)
 ───────────────▶│                  │
                 └───────┬──────────┘
                         │ valida por HTTP
            ┌────────────┴─────────────┐
            ▼                          ▼
 ┌──────────────────┐        ┌──────────────────┐
 │ usuarios-service │ :8081  │ productos-service│ :8082
 └────────┬─────────┘        └─────────┬────────┘
          ▼                            ▼
     usuarios-db                  productos-db
     (PostgreSQL)                 (PostgreSQL)
```

- **usuarios-service** (`:8081`) → `usuarios-db`
- **productos-service** (`:8082`) → `productos-db`
- **pedidos-service** (`:8083`) → `pedidos-db`. Antes de guardar un pedido, valida por HTTP
  que el usuario y el producto existan, y que haya stock suficiente.

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

## 2. Probar usuarios — http://localhost:8081

```bash
# Listar usuarios (vienen 4 de ejemplo)
curl http://localhost:8081/usuarios

# Obtener uno
curl http://localhost:8081/usuarios/1

# Crear usuario
curl -X POST http://localhost:8081/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Nuevo Usuario", "email": "nuevo@example.com"}'
```

## 3. Probar productos — http://localhost:8082

```bash
# Listar productos (vienen 2 de ejemplo)
curl http://localhost:8082/productos

# Obtener uno
curl http://localhost:8082/productos/1

# Crear producto
curl -X POST http://localhost:8082/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Monitor 24", "precio": 129.99, "stock": 15}'
```

## 4. Crear pedidos — http://localhost:8083

```bash
# Crear pedido válido (usuario 1 compra 2 unidades del producto 1)
curl -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 2}'

# Listar pedidos
curl http://localhost:8083/pedidos

# Validación: usuario inexistente -> 400 Bad Request
curl -i -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 999, "productoId": 1, "cantidad": 1}'

# Validación de stock -> 400 Bad Request
curl -i -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 99999}'
```

En **Postman**: para los `POST` usa Body → raw → JSON con el cuerpo indicado.
Un pedido válido devuelve `201 Created` con `nombreUsuario`, `nombreProducto` y `total`.

## 5. Endpoints disponibles

| Servicio          | Método | Ruta              |
|-------------------|--------|-------------------|
| usuarios (:8081)  | GET    | `/usuarios`       |
| usuarios          | GET    | `/usuarios/{id}`  |
| usuarios          | POST   | `/usuarios`       |
| productos (:8082) | GET    | `/productos`      |
| productos         | GET    | `/productos/{id}` |
| productos         | POST   | `/productos`      |
| pedidos (:8083)   | GET    | `/pedidos`        |
| pedidos           | GET    | `/pedidos/{id}`   |
| pedidos           | POST   | `/pedidos`        |

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
- `pedidos-service` descubre a los otros servicios por su **nombre de servicio** dentro
  de la red de Docker (`http://usuarios-service:8081`). Para hablar con su base usa
  `jdbc:postgresql://pedidos-db:5432/pedidos`. Dentro de Docker nunca se usa `localhost`.
- Cada servicio tiene su propia base: no comparten tablas (database per service).
- No incluye aún: Kubernetes, Kafka, API Gateway ni seguridad (siguientes pasos).
