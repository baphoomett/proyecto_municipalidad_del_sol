ms_alertas
===========

Propósito
- Recibir eventos de nuevos focos de incendio (vía RabbitMQ o webhook HTTP) y emitir alertas por email y SMS (simulado) a la comunidad/autoridades.

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
