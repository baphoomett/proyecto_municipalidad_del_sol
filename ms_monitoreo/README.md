ms_monitoreo
===========

Propósito
- Visualización y seguimiento de focos de incendio en mapas interactivos.

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
