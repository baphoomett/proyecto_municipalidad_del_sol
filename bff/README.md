bff
===

Propósito
- Backend for Frontend: expone al frontend una API simplificada bajo `/bff/**`, agrega CORS para el origen del frontend y reenvía cada llamada al `api_gateway` (que a su vez rutea al microservicio interno correspondiente).
- Centraliza el manejo del JWT del usuario (lo recibe del frontend y lo reenvía como `Authorization: Bearer <token>` hacia el gateway) y aplica validaciones de rol antes de reenviar operaciones sensibles.

Módulos del proyecto
---------------------
Este servicio es parte de un sistema mayor compuesto por:
- `frontend` — SPA en React/Vite: login, dashboard, mapa de focos, reportes, alertas y panel de administración.
- `bff` (este módulo) — Backend for Frontend: API simplificada para el frontend, agrega CORS y valida roles antes de reenviar al gateway.
- `api_gateway` — Punto único de entrada al backend: rutea cada request al microservicio interno correspondiente.
- `ms_usuarios` — Autenticación, datos personales y roles/permisos.
- `ms_reportes` — Gestión de reportes de incendios y su ciclo de vida.
- `ms_monitoreo` — Seguimiento geoespacial de focos activos en tiempo real (SSE).
- `ms_alertas` — Emisión de alertas por email y SMS (simulado) ante nuevos focos.
- `ms_integracion` — Integración con MinIO (evidencia) y RabbitMQ.

Decisiones arquitectónicas
- Patrón BFF: el frontend nunca llama directo a los microservicios ni al `api_gateway`; siempre pasa por este servicio.
- Comunicación con el backend vía `RestTemplate`/`WebClient` apuntando a `api.gateway.url` (`API_GATEWAY_URL` en Docker).
- CORS configurado explícitamente para el origen del frontend (`http://localhost:5173` en desarrollo).

Endpoints expuestos al frontend
- `POST /bff/auth/register` — proxy hacia `ms_usuarios` (vía gateway). Público.
- `POST /bff/auth/login` — proxy hacia `ms_usuarios` (vía gateway). Público.
- `POST /bff/reports` — proxy hacia `ms_reportes` (vía gateway). Requiere JWT.
- `GET /bff/reports` — proxy hacia `ms_reportes` (vía gateway). Requiere JWT.
- `PATCH /bff/reports/{id}/status` — proxy hacia `ms_reportes` (vía gateway). Requiere JWT con rol `ROLE_ADMIN`; si el usuario no es admin, el bff responde `403 Forbidden` sin llegar a reenviar la petición.
- `GET /bff/alerts` — proxy hacia `ms_alertas` (vía gateway). Requiere JWT.

Configuración
- `src/main/resources/application.properties`: `api.gateway.url` (URL del `api_gateway`) y `jwt.secret` (debe coincidir con el de `ms_usuarios`).

Instalación
-----------
Requisitos: Java 21 y Maven (o el wrapper `./mvnw` / `mvnw.cmd` incluido en el proyecto).

```bash
cd bff
mvn clean install
```

Ejecución
---------
Opción A — con Docker Compose (recomendado, depende de que `api_gateway` esté arriba):

```bash
cd ..
docker-compose up -d --build api_gateway bff
```

El servicio queda disponible en `http://localhost:8086`.

Opción B — standalone (requiere el `api_gateway` accesible; configurar `API_GATEWAY_URL` si no corre en `http://api_gateway:8085`):

```bash
mvn clean package
java -jar target/bff-0.0.1-SNAPSHOT.jar
```

Pruebas
-------
Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `bff/`. Actualmente solo existe el smoke test de contexto Spring Boot (`BffApplicationTests.contextLoads`); no hay pruebas unitarias de servicio/controlador en este módulo.
