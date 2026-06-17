ms_monitoreo
===========

Propósito
- Visualización y seguimiento de focos de incendio en mapas interactivos.

Módulos del proyecto
---------------------
Este microservicio es parte de un sistema mayor compuesto por:
- `frontend` — SPA en React/Vite: login, dashboard, mapa de focos, reportes, alertas y panel de administración.
- `bff` — Backend for Frontend: API simplificada para el frontend, agrega CORS y valida roles antes de reenviar al gateway.
- `api_gateway` — Punto único de entrada al backend: rutea cada request al microservicio interno correspondiente.
- `ms_usuarios` — Autenticación, datos personales y roles/permisos.
- `ms_reportes` — Gestión de reportes de incendios y su ciclo de vida.
- `ms_monitoreo` (este módulo) — Seguimiento geoespacial de focos activos en tiempo real (SSE).
- `ms_alertas` — Emisión de alertas por email y SMS (simulado) ante nuevos focos.
- `ms_integracion` — Integración con MinIO (evidencia) y RabbitMQ.

Qué implementé
- Entidades: `Focus` (sin lat/long — usa `geometry` GeoJSON), `Brigade`.
- Repositorios JPA: `FocusRepository`, `BrigadeRepository`.
- Servicio `FocusService` con patrón Factory (`FocusFactory`) para crear `Focus`.
- Endpoints:
  - `POST /api/monitor/focus` — crear foco (JSON `CreateFocusRequest`).
  - `GET /api/monitor/focus` — listar focos (filtro por `status`, paginado).
  - `GET /api/monitor/focus/{id}` — obtener foco por id.
  - `GET /api/monitor/stream` — SSE stream para recibir eventos en tiempo real (`focus-created`).

Notas
- SSE: Clientes pueden suscribirse a `/api/monitor/stream` para recibir notificaciones en tiempo real cuando se cree un nuevo foco.
- Integrar con `ms_reportes`: se puede hacer que `ms_reportes` llame `POST /api/monitor/focus` al crear un `Report`.

Instalación
-----------
Requisitos: Java 21, Maven (o el wrapper `./mvnw` / `mvnw.cmd`) y una base PostgreSQL con extensión PostGIS.

```bash
cd ms_monitoreo
mvn clean install
```

Ejecución
---------
Opción A — con Docker Compose (recomendado, levanta este servicio junto con su base PostgreSQL/PostGIS y RabbitMQ):

```bash
cd ..
docker-compose up -d --build postgres_ms_monitoreo rabbitmq ms_monitoreo
```

El servicio queda disponible en `http://localhost:8182` (puerto interno 8080).

Opción B — standalone (requiere PostgreSQL/PostGIS y RabbitMQ accesibles; configurar `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_RABBITMQ_HOST` y `SPRING_FLYWAY_ENABLED=true`):

```bash
mvn clean package
java -jar target/ms_monitoreo-0.0.1-SNAPSHOT.jar
```

Pruebas unitarias
-----------------

Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `ms_monitoreo/`.

Resumen de la última ejecución: **13/14 pruebas pasaron**. La única falla (`MsMonitoreoApplicationTests.contextLoads`) es una prueba de contexto Spring Boot ya existente en el proyecto que requiere una base PostgreSQL/PostGIS real corriendo (vía `docker-compose`); no es una prueba unitaria y falla por falta de infraestructura, no por un defecto de código.

### `FocusFactoryTest` (pruebas unitarias puras sobre `FocusFactory`, sin mocks)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `createFocus_conWktValidoYSeveridadValida_deberiaCrearFocoConGeometriaYSeveridad` | Unitaria | Con un WKT válido (`POINT(...)`) y severidad válida, se crea un `Focus` con la geometría parseada y la severidad correcta | `Focus` creado con geometría tipo `Point` y severidad `HIGH` | ✅ Pasó |
| 2 | `createFocus_conWktInvalido_deberiaLanzarIllegalArgumentException` | Unitaria (caso de error) | Un WKT inválido lanza `IllegalArgumentException` con mensaje descriptivo | Se lanzó `IllegalArgumentException` con mensaje `"Invalid geometry WKT: ..."` | ✅ Pasó |
| 3 | `createFocus_conSeveridadInvalida_deberiaUsarMediumPorDefecto` | Unitaria (caso límite) | Una severidad fuera del enum cae al valor por defecto `MEDIUM` | Severidad resultante: `MEDIUM` | ✅ Pasó |
| 4 | `createFocus_conSeveridadNula_deberiaUsarMediumPorDefecto` | Unitaria (caso límite) | Severidad nula cae al valor por defecto `MEDIUM` | Severidad resultante: `MEDIUM` | ✅ Pasó |
| 5 | `createFocus_sinGeometria_deberiaCrearFocoConGeometriaNula` | Unitaria (caso límite) | Sin geometría en la solicitud, el `Focus` se crea con geometría `null` (no lanza excepción) | `Focus` creado con `geometry = null` | ✅ Pasó |

### `FocusServiceTest` (pruebas unitarias con Mockito sobre `FocusService`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `createFocus_conDatosValidos_deberiaGuardarYPublicarEnRabbitMQ` | Unitaria (mocks de `FocusRepository`, `AmqpTemplate`) | Al crear un foco, se guarda en el repositorio y se publica un mensaje en el exchange `alerts.exchange` con routing `alerts.new` | Foco guardado con id, `convertAndSend` invocado una vez con el exchange/routing correctos | ✅ Pasó |
| 2 | `createFocus_siRabbitMQFalla_noDeberiaPropagarLaExcepcion` | Unitaria (resiliencia) | Si la publicación en RabbitMQ falla, la creación del foco no debe fallar | El foco se creó correctamente a pesar del error de publicación | ✅ Pasó |
| 3 | `listFocus_sinFiltro_deberiaRetornarTodos` | Unitaria (mock de `FocusRepository`) | Sin filtro de estado, se listan todos los focos paginados | Retornó 2 focos; `findByStatus` nunca invocado | ✅ Pasó |
| 4 | `listFocus_conFiltroValido_deberiaFiltrarPorStatus` | Unitaria (mock de `FocusRepository`) | Con un estado válido, se filtra por dicho estado | Retornó 1 foco filtrado por `VERIFIED` | ✅ Pasó |
| 5 | `listFocus_conFiltroInvalido_deberiaRetornarTodosSinFallar` | Unitaria (resiliencia) | Con un estado inexistente en el enum, se listan todos sin lanzar excepción | Retornó todos los focos sin error | ✅ Pasó |
| 6 | `getById_focoExistente_deberiaRetornarloEnvueltoEnOptional` | Unitaria | Un foco existente se retorna envuelto en `Optional` | Retornó `Optional` con el foco (`id=5`) | ✅ Pasó |
| 7 | `getById_focoInexistente_deberiaRetornarOptionalVacio` | Unitaria | Un foco inexistente retorna `Optional.empty()` | Retornó vacío | ✅ Pasó |
| 8 | `subscribe_deberiaRetornarUnSseEmitterNoNulo` | Unitaria | `subscribe()` retorna un `SseEmitter` no nulo, listo para recibir eventos | Se retornó una instancia válida de `SseEmitter` | ✅ Pasó |

### Prueba preexistente

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `MsMonitoreoApplicationTests.contextLoads` | Smoke test de contexto Spring Boot (`@SpringBootTest`) | El contexto de la aplicación carga correctamente | Falla al crear el `DataSource`: no hay un driver/URL de base de datos configurado para pruebas (requiere PostgreSQL/PostGIS real vía `docker-compose`) | ❌ Falló (requiere infraestructura externa, no es una prueba unitaria) |
