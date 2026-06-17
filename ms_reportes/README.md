ms_reportes
===========

Propósito
- Gestionar reportes de incendios y eventos asociados (CREATED, DISPATCHED, CLOSED).

Decisiones principales
- Cada microservicio tiene su propia BD PostgreSQL.
- Persistencia: Spring Data JPA con `ReportRepository` y `EventRepository` (Repository Pattern).
- `Report` almacena `mediaUrls` como `@ElementCollection` para guardar URLs de imágenes.
- Creation de entidades: `ReportFactory.createReport` y `ReportFactory.createEvent` (Factory Method).
- Circuit Breaker: se recomienda usar `resilience4j` en llamadas salientes (p.ej. notificar a `ms_alertas` o `ms_usuarios`).

Endpoints implementados
- `POST /api/reports` — crear reporte (cuerpo: `reporterEmail`, `latitude`, `longitude`, `description`, `mediaUrls`).
- `GET /api/reports` — listar reportes (query: `status`, `page`, `size`).
- `POST /api/reports/{id}/events` — agregar evento al reporte (`type`, `payload`).

- `POST /api/reports/form` — crear reporte usando `multipart/form-data` con campos `reporterEmail`, `latitude`, `longitude`, `description` y archivos `files` (solo PNG/JPEG, máximo 50 MB por archivo). Files se almacenan localmente en el directorio configurado `file.upload-dir` y son servidos desde `/media/{filename}`.

Notas
- Actualmente no hay verificación de JWT entre microservicios; integrar validación de tokens compartiendo secret o via OAuth2/JWK es sugerido.
- Multimedia: actualmente solo se guardan URLs. Implementar almacenamiento (MinIO/S3) y flow de presigned URLs más adelante.

Pruebas unitarias
-----------------

Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `ms_reportes/`.

Resumen de la última ejecución: **19/20 pruebas pasaron**. La única falla (`MsReportesApplicationTests.contextLoads`) es una prueba de contexto Spring Boot ya existente en el proyecto que requiere una base PostgreSQL real corriendo (vía `docker-compose`); no es una prueba unitaria y falla por falta de infraestructura, no por un defecto de código.

### `ReportServiceTest` (pruebas unitarias con Mockito sobre `ReportService`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `createReport_deberiaGuardarReporteYEventoInicial` | Unitaria (mocks de `ReportRepository`, `EventRepository`) | Al crear un reporte, se guarda el `Report` y se crea un `Event` inicial `CREATED`, quedando el estado en `ACTIVO` | Reporte guardado con id, estado `ACTIVO`, severidad y tipo de incidente correctos; ambos repositorios invocados una vez | ✅ Pasó |
| 2 | `createReport_deberiaNotificarAMonitoreoYAlertas` | Unitaria (mocks de `WebhookService`, `AlertWebhookService`) | Al crear un reporte, se notifica a `ms_monitoreo` y a `ms_alertas` | `notifyMonitor` y `notifyAlertas` invocados una vez cada uno | ✅ Pasó |
| 3 | `createReport_noDeberiaFallarSiNotificacionMonitorLanzaExcepcion` | Unitaria (resiliencia) | Si la notificación a `ms_monitoreo` lanza una excepción, la creación del reporte no debe fallar | El reporte se creó correctamente a pesar del error de notificación | ✅ Pasó |
| 4 | `createReport_noDeberiaFallarSiNotificacionAlertasLanzaExcepcion` | Unitaria (resiliencia) | Si la notificación a `ms_alertas` lanza una excepción, la creación del reporte no debe fallar | El reporte se creó correctamente a pesar del error de notificación | ✅ Pasó |
| 5 | `listReports_sinFiltro_deberiaRetornarTodos` | Unitaria (mock de `ReportRepository`) | Sin filtro de estado, se listan todos los reportes paginados | Retornó 2 reportes; `findByStatus` nunca invocado | ✅ Pasó |
| 6 | `listReports_conFiltroValido_deberiaFiltrarPorStatus` | Unitaria (mock de `ReportRepository`) | Con un estado válido, se filtra por dicho estado | Retornó 1 reporte filtrado por `ACTIVO` | ✅ Pasó |
| 7 | `listReports_conFiltroInvalido_deberiaRetornarTodosSinFallar` | Unitaria (resiliencia) | Con un estado inexistente en el enum, se listan todos sin lanzar excepción | Retornó todos los reportes sin error | ✅ Pasó |
| 8 | `addEvent_reporteExistente_deberiaGuardarEventoYActualizarStatusADespachado` | Unitaria (mock de `ReportRepository`, `EventRepository`) | Un evento `DISPATCHED` sobre un reporte existente lo guarda y cambia el estado a `EN_COMBATE` | Evento guardado y estado actualizado correctamente | ✅ Pasó |
| 9 | `addEvent_eventoClosed_deberiaActualizarStatusAExtinguido` | Unitaria | Un evento `CLOSED` cambia el estado del reporte a `EXTINGUIDO` | Estado actualizado a `EXTINGUIDO` | ✅ Pasó |
| 10 | `addEvent_reporteInexistente_deberiaRetornarOptionalVacio` | Unitaria | Agregar un evento a un reporte inexistente retorna `Optional.empty()` sin guardar nada | Retornó vacío; `eventRepository.save` nunca invocado | ✅ Pasó |
| 11 | `updateStatus_reporteExistenteYEstadoValido_deberiaActualizar` | Unitaria | Un estado válido (`CONTROLADO`) actualiza el reporte | Reporte actualizado con el nuevo estado | ✅ Pasó |
| 12 | `updateStatus_estadoInvalido_deberiaRetornarOptionalVacio` | Unitaria (resiliencia) | Un estado que no existe en el enum retorna `Optional.empty()` sin guardar | Retornó vacío; `reportRepository.save` nunca invocado | ✅ Pasó |
| 13 | `updateStatus_reporteInexistente_deberiaRetornarOptionalVacio` | Unitaria | Actualizar el estado de un reporte inexistente retorna `Optional.empty()` | Retornó vacío | ✅ Pasó |

### `ReportControllerTest` (pruebas de slice `@WebMvcTest` sobre `ReportController`, con `ReportService` y `UploadService` mockeados vía `@MockitoBean`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `createReport_conDatosValidos_deberiaRetornar201YElReporteCreado` | Integración de capa web (MockMvc) | `POST /api/reports` retorna `201 Created`, header `Location` con la URL del recurso y el reporte creado en el cuerpo | Retornó `201 Created`, `Location: /api/reports/1`, cuerpo con `id=1` y `status=ACTIVO` | ✅ Pasó |
| 2 | `listReports_sinFiltro_deberiaRetornar200ConPaginaDeReportes` | Integración de capa web (MockMvc) | `GET /api/reports` retorna `200 OK` con una página de reportes serializada en JSON | Retornó `200 OK` con `content.length() == 2` | ✅ Pasó |
| 3 | `addEvent_reporteExistente_deberiaRetornar200ConElEvento` | Integración de capa web (MockMvc) | `POST /api/reports/{id}/events` sobre un reporte existente retorna `200 OK` con el evento creado | Retornó `200 OK` con el evento (`id=10`) | ✅ Pasó |
| 4 | `addEvent_reporteInexistente_deberiaRetornar404` | Integración de capa web (MockMvc) | `POST /api/reports/{id}/events` sobre un reporte inexistente retorna `404 Not Found` | Retornó `404 Not Found` | ✅ Pasó |
| 5 | `updateStatus_estadoValido_deberiaRetornar200ConElReporteActualizado` | Integración de capa web (MockMvc) | `PATCH /api/reports/{id}/status` con un estado válido retorna `200 OK` con el reporte actualizado | Retornó `200 OK` con `status=CONTROLADO` | ✅ Pasó |
| 6 | `updateStatus_estadoInvalidoOReporteInexistente_deberiaRetornar404` | Integración de capa web (MockMvc) | `PATCH /api/reports/{id}/status` con un estado inválido o reporte inexistente retorna `404 Not Found` | Retornó `404 Not Found` | ✅ Pasó |

### Prueba preexistente

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `MsReportesApplicationTests.contextLoads` | Smoke test de contexto Spring Boot (`@SpringBootTest`) | El contexto de la aplicación carga correctamente | Falla al crear el `DataSource`: no hay un driver/URL de base de datos configurado para pruebas (requiere PostgreSQL real vía `docker-compose`) | ❌ Falló (requiere infraestructura externa, no es una prueba unitaria) |
