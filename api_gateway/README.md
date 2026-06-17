api_gateway
===========

Propósito
- Puerta de entrada única al backend (Spring Cloud Gateway): recibe todas las peticiones bajo `/api/**` y las rutea al microservicio interno correspondiente según el path.
- Desacopla a los clientes (el `bff`, o cualquier cliente HTTP directo) de las direcciones internas de cada microservicio en la red Docker.

Módulos del proyecto
---------------------
Este servicio es parte de un sistema mayor compuesto por:
- `frontend` — SPA en React/Vite: login, dashboard, mapa de focos, reportes, alertas y panel de administración.
- `bff` — Backend for Frontend: API simplificada para el frontend, agrega CORS y valida roles antes de reenviar al gateway.
- `api_gateway` (este módulo) — Punto único de entrada al backend: rutea cada request al microservicio interno correspondiente.
- `ms_usuarios` — Autenticación, datos personales y roles/permisos.
- `ms_reportes` — Gestión de reportes de incendios y su ciclo de vida.
- `ms_monitoreo` — Seguimiento geoespacial de focos activos en tiempo real (SSE).
- `ms_alertas` — Emisión de alertas por email y SMS (simulado) ante nuevos focos.
- `ms_integracion` — Integración con MinIO (evidencia) y RabbitMQ.

Decisiones arquitectónicas
- Ruteo declarativo con `RouteLocator` (`GatewayConfig`), sin lógica de negocio propia.
- Las rutas apuntan a las IPs fijas de la red Docker (`municipalidad_net`), no a nombres de servicio, por lo que requiere que esa red exista con esas IPs (ver `docker-compose.yml`).

Rutas configuradas
- `/api/auth/**` → `ms_usuarios` (`172.20.0.10:8080`)
- `/api/reports/**` → `ms_reportes` (`172.20.0.11:8080`)
- `/api/monitor/**` → `ms_monitoreo` (`172.20.0.12:8080`)
- `/api/alerts/**` → `ms_alertas` (`172.20.0.13:8083`)
- `/api/integration/**` → `ms_integracion` (`172.20.0.14:8090`)

Nota: el prefijo `/api/integration/**` no coincide con los endpoints reales de `ms_integracion` (`/api/minio/**`, `/api/health`), por lo que esa ruta en particular no funciona tal como está; para probar `ms_integracion` se accede directo al puerto `8090` (ver su propio README).

Configuración
- `src/main/resources/application.yml`: nombre de la aplicación y `server.port` (8085).

Instalación
-----------
Requisitos: Java 21 y Maven (o el wrapper `./mvnw` / `mvnw.cmd` incluido en el proyecto).

```bash
cd api_gateway
mvn clean install
```

Ejecución
---------
Opción A — con Docker Compose (recomendado, depende de que los 5 microservicios estén arriba):

```bash
cd ..
docker-compose up -d --build ms_usuarios ms_reportes ms_monitoreo ms_alertas ms_integracion api_gateway
```

El servicio queda disponible en `http://localhost:8085`.

Opción B — standalone (requiere los microservicios accesibles en las IPs configuradas en `GatewayConfig`, lo que en la práctica solo funciona dentro de la red Docker del proyecto):

```bash
mvn clean package
java -jar target/api_gateway-0.0.1-SNAPSHOT.jar
```

Pruebas
-------
Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `api_gateway/`. Actualmente solo existe el smoke test de contexto Spring Boot (`ApiGatewayApplicationTests.contextLoads`); no hay pruebas unitarias propias en este módulo.
