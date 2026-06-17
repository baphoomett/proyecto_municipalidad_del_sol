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

## Pruebas unitarias

Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `ms_integracion/`.

Resumen de la última ejecución: **7/8 pruebas pasaron**. La única falla (`MsIntegracionApplicationTests.contextLoads`) es una prueba de contexto Spring Boot ya existente en el proyecto que requiere una base PostgreSQL real corriendo (vía `docker-compose`); no es una prueba unitaria y falla por falta de infraestructura, no por un defecto de código.

### `MinioServiceTest` (pruebas unitarias con Mockito sobre `MinioService`, mockeando `MinioClient`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `upload_conDatosValidos_deberiaInvocarPutObjectEnMinioClient` | Unitaria (mock de `MinioClient`) | `upload(...)` invoca `MinioClient.putObject` con los argumentos correctos | `putObject` invocado una vez | ✅ Pasó |
| 2 | `upload_siMinioClientFalla_deberiaPropagarLaExcepcion` | Unitaria (caso de error) | Si `MinioClient.putObject` falla, la excepción se propaga al llamador | Se propagó `RuntimeException("MinIO no disponible")` | ✅ Pasó |
| 3 | `generatePresignedUrl_conDatosValidos_deberiaRetornarLaUrlDeMinioClient` | Unitaria (mock de `MinioClient`) | `generatePresignedUrl(...)` retorna la URL generada por `MinioClient.getPresignedObjectUrl` | Retornó la URL simulada `https://minio.local/integracion/evidencias/foto1.jpg?sig=abc` | ✅ Pasó |

### `IntegrationControllerTest` (pruebas de slice `@WebMvcTest` sobre `IntegrationController`, con `MinioService` mockeado vía `@MockitoBean`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `presigned_conObjectNameValido_deberiaRetornar200ConLaUrl` | Integración de capa web (MockMvc) | `POST /api/minio/presigned` con `objectName` válido retorna `200 OK` con la URL en `$.url` | Retornó `200 OK` con `url = "https://minio.local/presigned-url"` | ✅ Pasó |
| 2 | `presigned_sinObjectName_deberiaRetornar400` | Integración de capa web (MockMvc) | Sin `objectName`, retorna `400 Bad Request` con mensaje de error | Retornó `400 Bad Request` con `error = "objectName is required"` | ✅ Pasó |
| 3 | `presigned_siMinioServiceFalla_deberiaRetornar500` | Integración de capa web (MockMvc) | Si `MinioService` lanza una excepción, retorna `500 Internal Server Error` con el mensaje de error | Retornó `500` con `error = "MinIO no disponible"` | ✅ Pasó |
| 4 | `health_deberiaRetornar200ConStatusUp` | Integración de capa web (MockMvc) | `GET /api/health` retorna `200 OK` con `{"status":"UP"}` | Retornó `200 OK` con `status = "UP"` | ✅ Pasó |

### Prueba preexistente

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `MsIntegracionApplicationTests.contextLoads` | Smoke test de contexto Spring Boot (`@SpringBootTest`) | El contexto de la aplicación carga correctamente | Falla al crear el `DataSource`: no hay un driver/URL de base de datos configurado para pruebas (requiere PostgreSQL real vía `docker-compose`) | ❌ Falló (requiere infraestructura externa, no es una prueba unitaria) |
