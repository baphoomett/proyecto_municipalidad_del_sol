# ms_integracion

Microservicio de integración ligero para el sistema.

Características incluidas:

- RabbitMQ: exchange `integracion.exchange`, queue `integracion.queue`, routing `integracion.routing`.
- Consumer sencillo que registra eventos JSON con forma `{ "tipo":..., "zona":..., "mensaje":... }`.
- MinIO: cliente simple con `upload` y `generatePresignedUrl` (endpoints para probar).
- Resilience4j: `Retry` y `CircuitBreaker` aplicados a operaciones MinIO.
- Endpoints de prueba: `POST /api/minio/presigned` y `GET /api/health`.

## Requisitos

- Java 21
- Maven (o usar `./mvnw` desde la carpeta del proyecto raíz)
- RabbitMQ (con plugin Management opcional en `:15672`)
- MinIO (opcional para probar presigned URLs)

## Build

Desde la raíz del repo (el POM raíz incluye `ms_integracion`):

```powershell
.\mvnw -pl ms_integracion clean package -DskipTests
```

O directamente dentro del módulo:

```powershell
cd ms_integracion
.\mvnw clean package -DskipTests
```

## Ejecutar localmente

1. Arranca RabbitMQ y MinIO (por ejemplo con Docker). Ejemplo mínimo:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name minio -p 9000:9000 -e MINIO_ROOT_USER=minioadmin -e MINIO_ROOT_PASSWORD=minioadmin minio/minio server /data
```

2. Ejecuta la aplicación (desde `ms_integracion`):

```powershell
cd ms_integracion
.\mvnw spring-boot:run
```

3. Publicar evento de prueba (requiere Management API activo):

```bash
./scripts/publish_event.sh
# o en PowerShell
.\scripts\publish_event.ps1
```

4. Verifica logs: `EventConsumer` imprimirá el evento recibido.

## Probar MinIO presigned URL

POST a `http://localhost:8090/api/minio/presigned` con JSON:

```json
{ "objectName": "evidencias/imagen-1.jpg", "expirySeconds": 3600 }
```

Respuesta: `{ "url": "..." }` (requiere MinIO accesible y bucket configurado).

## Notas sobre Flyway y PostGIS

Si tu entorno usa H2 u otra base embebida para tests, la migración que crea la extensión PostGIS se implementó como una migración Java que solo ejecuta `CREATE EXTENSION` cuando la base de datos es PostgreSQL. Esto evita errores de sintaxis en DBs que no soportan `CREATE EXTENSION`.
