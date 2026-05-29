# JavaDockerLab — 3 microservicios con Spring Boot + Docker

Proyecto de práctica para entender microservicios y Docker de forma simple.
**Sin base de datos**: cada servicio guarda los datos en memoria (se pierden al reiniciar).

## Arquitectura

```
                 ┌──────────────────┐
   crea pedido   │  pedidos-service │  :8083
 ───────────────▶│                  │
                 └───────┬──────────┘
                         │ valida por HTTP
            ┌────────────┴─────────────┐
            ▼                          ▼
 ┌──────────────────┐        ┌──────────────────┐
 │ usuarios-service │ :8081  │ productos-service│ :8082
 └──────────────────┘        └──────────────────┘
```

- **usuarios-service** (`:8081`): CRUD básico de usuarios en memoria.
- **productos-service** (`:8082`): CRUD básico de productos en memoria (con `stock`).
- **pedidos-service** (`:8083`): crea pedidos. Antes de guardar, valida por HTTP que
  el usuario y el producto existan, y que haya stock suficiente.

Cada servicio tiene la misma estructura sencilla:

```
servicio/
├── Dockerfile                 # build multi-stage (compila con Maven dentro del contenedor)
├── pom.xml
└── src/main/
    ├── java/com/lab/<servicio>/
    │   ├── controller/        # endpoints REST
    │   ├── service/           # lógica + datos en memoria
    │   ├── model/             # entidades
    │   ├── dto/               # objetos de request
    │   └── client/            # (solo pedidos) llamadas HTTP a otros servicios
    └── resources/application.properties
```

## Requisitos

Para levantar todo con Docker **solo necesitas Docker** (Docker Desktop).
No necesitas Java ni Maven instalados: el `Dockerfile` compila cada servicio
dentro del contenedor usando una imagen de Maven (build multi-stage).

> Si prefieres correr un servicio suelto sin Docker, necesitarías JDK 17 + Maven.

## Levantar todo con Docker Compose

Desde la raíz del proyecto (`JavaDockerLab/`):

```bash
# Construir las imágenes y levantar los 3 servicios
docker compose up --build

# (en segundo plano)
docker compose up --build -d

# Ver logs
docker compose logs -f pedidos-service

# Apagar todo
docker compose down
```

La primera vez tarda un poco porque descarga dependencias de Maven.

## Endpoints

### usuarios-service — http://localhost:8081
| Método | Ruta             | Descripción            |
|--------|------------------|------------------------|
| GET    | `/usuarios`      | Listar usuarios        |
| GET    | `/usuarios/{id}` | Obtener un usuario     |
| POST   | `/usuarios`      | Crear usuario          |

### productos-service — http://localhost:8082
| Método | Ruta              | Descripción            |
|--------|-------------------|------------------------|
| GET    | `/productos`      | Listar productos       |
| GET    | `/productos/{id}` | Obtener un producto    |
| POST   | `/productos`      | Crear producto         |

### pedidos-service — http://localhost:8083
| Método | Ruta            | Descripción                          |
|--------|-----------------|--------------------------------------|
| GET    | `/pedidos`      | Listar pedidos                       |
| GET    | `/pedidos/{id}` | Obtener un pedido                    |
| POST   | `/pedidos`      | Crear pedido (valida usuario+producto) |

Cada servicio arranca con datos de ejemplo (2 usuarios y 2 productos con `id` 1 y 2).

## Probar el flujo completo (curl / Postman)

```bash
# 1. Ver usuarios y productos precargados
curl http://localhost:8081/usuarios
curl http://localhost:8082/productos

# 2. Crear un pedido válido (usuario 1 compra 2 unidades del producto 1)
curl -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 2}'

# 3. Ver el pedido creado
curl http://localhost:8083/pedidos

# 4. Probar validación: usuario inexistente -> 400 Bad Request
curl -i -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 999, "productoId": 1, "cantidad": 1}'

# 5. Probar validación de stock -> 400 Bad Request
curl -i -X POST http://localhost:8083/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "productoId": 1, "cantidad": 99999}'
```

### En Postman
1. Crea una colección con las requests de arriba.
2. Para los `POST`, usa **Body → raw → JSON** con el cuerpo indicado.
3. El pedido válido devuelve `201 Created` con `nombreUsuario`, `nombreProducto` y `total` calculado.

## Notas
- Los datos viven en memoria: al hacer `docker compose down` (o reiniciar) se borran.
- `pedidos-service` descubre a los otros servicios por su **nombre de servicio** dentro
  de la red de Docker (`http://usuarios-service:8081`), configurado vía variables de
  entorno en `docker-compose.yml`. Fuera de Docker usa `localhost` por defecto.
- Próximos pasos sugeridos: añadir base de datos (PostgreSQL), service discovery, o un API Gateway.
# Java-Docker-Lab
