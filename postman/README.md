# Postman Collection - Municipalidad Valle del Sol

Especificación práctica de la API REST del backend, lista para importar en Postman.

## Archivos

- `Municipalidad_Valle_del_Sol.postman_collection.json` — todos los endpoints, agrupados por microservicio, más una carpeta con las rutas del BFF tal como las consume el frontend.
- `Municipalidad_Valle_del_Sol.postman_environment.json` — variables de entorno (URLs locales, token, IDs de prueba).

## Importar

1. Abre Postman → **Import** → arrastra ambos archivos (o File > Import).
2. Selecciona el environment **"Municipalidad Valle del Sol - Local"** en el selector superior derecho.
3. Asegúrate de tener el backend levantado (`docker-compose up -d --build` desde la raíz del repo).

## Uso recomendado

1. **Auth (ms_usuarios) > Login** (o **Guest** si no quieres registrarte) — guarda el JWT automáticamente en la variable `token`.
2. **Reportes (ms_reportes) > Crear reporte (JSON)** — guarda automáticamente `report_id`.
3. **Monitoreo (ms_monitoreo) > Crear focus** — requiere JWT (ya seteado en el paso 1) y usa `report_id`; guarda automáticamente `focus_id`.
4. El resto de los endpoints protegidos ya usan `{{token}}` desde el header `Authorization`.

## Notas sobre autenticación por microservicio

| Microservicio | Requiere JWT |
|---|---|
| ms_usuarios | Sí (excepto `/api/auth/**`) |
| ms_reportes | No |
| ms_monitoreo | Sí (excepto `/actuator/**`) |
| ms_alertas | No |
| ms_integracion | No |
| BFF | Sí en `/bff/reports/**` y `/bff/alerts/**`; no en `/bff/auth/**` |

## Notas adicionales

- Las requests usan por defecto `{{gateway_url}}` (`http://localhost:8085`, el API Gateway). También existe la carpeta **BFF** para probar exactamente lo que consume el frontend (`http://localhost:8086`).
- `ms_integracion` se llama directo en el puerto `8090` porque el prefijo de ruteo configurado en el gateway (`/api/integration/**`) no coincide con el path real del controller (`/api/minio/**`, `/api/health`).
- El endpoint `GET /api/monitor/stream` es Server-Sent Events: la conexión queda abierta: Postman la muestra en streaming en la pestaña de respuesta.
- El endpoint `POST /api/reports/form` es `multipart/form-data`: selecciona los archivos en el campo `files` antes de enviar.
