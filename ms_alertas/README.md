ms_alertas
===========

Propósito
- Recibir eventos de nuevos focos de incendio (vía RabbitMQ o webhook HTTP) y emitir alertas por email y SMS (simulado) a la comunidad/autoridades.

Módulos del proyecto
---------------------
Este microservicio es parte de un sistema mayor compuesto por:
- `frontend` — SPA en React/Vite: login, dashboard, mapa de focos, reportes, alertas y panel de administración.
- `bff` — Backend for Frontend: API simplificada para el frontend, agrega CORS y valida roles antes de reenviar al gateway.
- `api_gateway` — Punto único de entrada al backend: rutea cada request al microservicio interno correspondiente.
- `ms_usuarios` — Autenticación, datos personales y roles/permisos.
- `ms_reportes` — Gestión de reportes de incendios y su ciclo de vida.
- `ms_monitoreo` — Seguimiento geoespacial de focos activos en tiempo real (SSE).
- `ms_alertas` (este módulo) — Emisión de alertas por email y SMS (simulado) ante nuevos focos.
- `ms_integracion` — Integración con MinIO (evidencia) y RabbitMQ.

Decisiones principales
- Consumo asíncrono de eventos desde RabbitMQ (`AlertListener`, cola definida en `RabbitConfig`).
- `AlertService.handleNewFocus` envía la alerta por `EmailSender` y `SmsSender`; usa `@Retry`/`@CircuitBreaker` (Resilience4j) para tolerar fallas transitorias de los proveedores.
- Si el procesamiento de un mensaje falla (tras agotar reintentos), se persiste en `FailedAlert` y `FailedAlertReprocessor` lo reintenta periódicamente (`@Scheduled`).
- `AlertController` expone `POST /api/alerts/webhook` como vía alternativa (HTTP) para recibir el evento y republicarlo en RabbitMQ, y `GET /api/alerts` para listar alertas guardadas.

Endpoints
- `POST /api/alerts/webhook` — recibe un nuevo foco (`CreateAlertRequest`), lo persiste como `Alert` y lo publica en `alerts.exchange`.
- `GET /api/alerts` — lista las alertas registradas.

Notas
- `SmsSender` simula el envío de SMS solo con logging (no depende de un proveedor externo como Twilio).
- En `docker-compose.yml`, este servicio usa Mailhog como servidor SMTP de prueba en lugar de SMTP real.

Instalación
-----------
Requisitos: Java 21, Maven (o el wrapper `./mvnw` / `mvnw.cmd`), una base PostgreSQL, RabbitMQ y un servidor SMTP (en desarrollo se usa MailHog).

```bash
cd ms_alertas
mvn clean install
```

Ejecución
---------
Opción A — con Docker Compose (recomendado, levanta este servicio junto con su base PostgreSQL, RabbitMQ y MailHog):

```bash
cd ..
docker-compose up -d --build postgres_ms_alertas rabbitmq mailhog ms_alertas
```

El servicio queda disponible en `http://localhost:8083` (puerto interno 8080). Los correos enviados se pueden revisar en la interfaz de MailHog, `http://localhost:8025`.

Opción B — standalone (requiere PostgreSQL, RabbitMQ y un servidor SMTP accesibles; configurar `SPRING_DATASOURCE_URL`, `SPRING_RABBITMQ_HOST` y las variables `SPRING_MAIL_*`):

```bash
mvn clean package
java -jar target/ms_alertas-0.0.1-SNAPSHOT.jar
```

Pruebas unitarias
-----------------

Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `ms_alertas/`.

Resumen de la última ejecución: **5/6 pruebas pasaron**. La única falla (`MsAlertasApplicationTests.contextLoads`) es una prueba de contexto Spring Boot ya existente en el proyecto que requiere una base PostgreSQL real corriendo (vía `docker-compose`); no es una prueba unitaria y falla por falta de infraestructura, no por un defecto de código.

### `AlertServiceTest` (pruebas unitarias con Mockito sobre `AlertService`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `handleNewFocus_deberiaEnviarEmailYSms` | Unitaria (mocks de `EmailSender`, `SmsSender`) | Al procesar un nuevo foco, se invoca tanto el envío de email como el de SMS | `emailSender.sendAlert` y `smsSender.sendAlert` invocados una vez cada uno | ✅ Pasó |
| 2 | `handleNewFocus_siEmailSenderFalla_deberiaPropagarExcepcionYNoEnviarSms` | Unitaria (caso de error) | Si el envío de email falla, la excepción se propaga y el SMS no se envía (se interrumpe el flujo) | Se propagó la excepción `"SMTP no disponible"`; `smsSender.sendAlert` nunca invocado | ✅ Pasó |

### `AlertListenerTest` (pruebas unitarias con Mockito sobre `AlertListener`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `onMessage_conPayloadValido_deberiaDelegarEnAlertService` | Unitaria (mock de `AlertService`) | Un mensaje válido se delega a `AlertService.handleNewFocus` y no se persiste como fallido | `handleNewFocus` invocado una vez; `failedAlertRepository.save` nunca invocado | ✅ Pasó |
| 2 | `onMessage_conPayloadNulo_deberiaIgnorarSinLlamarAlertService` | Unitaria (caso límite) | Un mensaje nulo se ignora silenciosamente, sin invocar el servicio ni persistir nada | Ni `handleNewFocus` ni `failedAlertRepository.save` fueron invocados | ✅ Pasó |
| 3 | `onMessage_siAlertServiceFalla_deberiaPersistirFailedAlertConElError` | Unitaria (caso de error, captura de argumento) | Si `AlertService` lanza una excepción, se persiste un `FailedAlert` con el `reportId`, el mensaje de error y el payload serializado | `FailedAlert` guardado con `reportId=7`, `error="ms_alertas: SMTP no disponible"` y payload JSON conteniendo `"reportId":7` | ✅ Pasó |

### Prueba preexistente

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `MsAlertasApplicationTests.contextLoads` | Smoke test de contexto Spring Boot (`@SpringBootTest`) | El contexto de la aplicación carga correctamente | Falla al crear el `DataSource`: no hay un driver/URL de base de datos configurado para pruebas (requiere PostgreSQL real vía `docker-compose`) | ❌ Falló (requiere infraestructura externa, no es una prueba unitaria) |
