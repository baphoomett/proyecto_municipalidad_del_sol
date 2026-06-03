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
