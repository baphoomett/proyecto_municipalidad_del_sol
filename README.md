# Municipalidad Valle del Sol

Sistema de gestión de reportes y alertas de incendios forestales, compuesto por una aplicación web y un backend de microservicios en Spring Boot. Permite a la ciudadanía reportar focos de incendio (con o sin registro previo), visualizarlos en un mapa en tiempo real, y notificar automáticamente por email/SMS a la comunidad y autoridades.

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Módulos](#módulos)
- [Tecnologías](#tecnologías)
- [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
- [Documentación adicional](#documentación-adicional)
- [Estructura del repositorio](#estructura-del-repositorio)

## Arquitectura

```
frontend  ->  bff  ->  api_gateway  ->  ms_usuarios / ms_reportes / ms_monitoreo / ms_alertas
                                                              |
                                            ms_reportes  --webhook-->  ms_monitoreo, ms_alertas
                                            ms_monitoreo --RabbitMQ-->  ms_alertas
```

- El **frontend** nunca llama a los microservicios directamente: pasa siempre por el **bff**.
- El **bff** agrega el JWT del usuario y reenvía al **api_gateway**, que rutea según el path (`/api/auth/**` → `ms_usuarios`, `/api/reports/**` → `ms_reportes`, etc.).
- `ms_reportes` notifica de forma asíncrona y no bloqueante a `ms_monitoreo` y `ms_alertas` cuando se crea un reporte.
- `ms_monitoreo` publica un evento en RabbitMQ cuando se crea un foco; `ms_alertas` lo consume y dispara las notificaciones (email/SMS simulado).
- `ms_integracion` es independiente del flujo de reportes/alertas: se usa para subir evidencia multimedia a MinIO.
- Cada microservicio con persistencia tiene su propia base de datos PostgreSQL (PostGIS en el caso de `ms_monitoreo`), siguiendo el patrón *database-per-service*.

## Módulos

| Módulo | Tipo | Puerto | Propósito |
|---|---|---|---|
| [`frontend`](frontend/README.md) | React + Vite | 5173 (dev) | SPA: login, dashboard, mapa de focos, reportes, alertas y panel de administración. |
| [`bff`](bff/README.md) | Spring Boot | 8086 | Backend for Frontend: expone una API simplificada (`/bff/**`), agrega CORS y valida roles antes de reenviar al gateway. |
| [`api_gateway`](api_gateway/README.md) | Spring Cloud Gateway | 8085 | Puerta de entrada única al backend: rutea cada request (`/api/**`) al microservicio interno correspondiente. |
| [`ms_usuarios`](ms_usuarios/README.md) | Spring Boot | 8084 | Autenticación, datos personales y roles/permisos. Emite JWT (login, registro, token `guest`). |
| [`ms_reportes`](ms_reportes/README.md) | Spring Boot | 8081 | Gestión de reportes de incendios y su ciclo de vida (eventos, estados, evidencia multimedia). |
| [`ms_monitoreo`](ms_monitoreo/README.md) | Spring Boot + PostGIS | 8182 | Seguimiento geoespacial de focos activos sobre un mapa, con notificaciones en tiempo real (SSE). |
| [`ms_alertas`](ms_alertas/README.md) | Spring Boot | 8083 | Emisión de alertas a la comunidad/autoridades por email y SMS (simulado) ante nuevos focos. |
| [`ms_integracion`](ms_integracion/README.md) | Spring Boot | 8090 | Integración con almacenamiento de evidencia (MinIO) y mensajería (RabbitMQ). |

## Tecnologías

- **Frontend**: React 19, React Router, Vite, Axios, Leaflet / react-leaflet.
- **Backend**: Java 21, Spring Boot, Spring Cloud Gateway, Spring Data JPA, Spring Security + JWT, Resilience4j.
- **Datos y mensajería**: PostgreSQL, PostGIS, RabbitMQ, MinIO.
- **Infraestructura local**: Docker / Docker Compose, MailHog (SMTP de prueba).
- **Testing**: JUnit 5, Mockito, MockMvc (`@WebMvcTest`).

## Cómo levantar el proyecto

Requisitos: Docker y Docker Compose.

```bash
docker-compose up -d --build
```

Esto levanta los 5 microservicios con sus bases de datos, RabbitMQ, MailHog, el `api_gateway` y el `bff`. Ver `docker-compose.yml` para el detalle de variables de entorno y puertos.

Para correr el frontend en modo desarrollo:

```bash
cd frontend
npm install
npm run dev
```

Cada módulo documenta también cómo instalarse y ejecutarse de forma standalone en su propio README (ver tabla de [Módulos](#módulos)).

## Documentación adicional

- **API REST**: especificación práctica en [`postman/`](postman/README.md) — Postman Collection con todos los endpoints, environment con variables locales, y ejemplos de petición/respuesta de la comunicación entre servicios en [`postman/ejemplos_comunicacion_servicios.txt`](postman/ejemplos_comunicacion_servicios.txt).
- **Pruebas unitarias**: código fuente compilado por componente en [`tests/codigo_pruebas_unitarias.md`](tests/codigo_pruebas_unitarias.md) y guía de ejecución/generación de reportes de cobertura en [`tests/GUIA_EJECUCION_PRUEBAS.md`](tests/GUIA_EJECUCION_PRUEBAS.md).

## Estructura del repositorio

```
proyecto_municipalidad_del_sol/
├── frontend/          # SPA React + Vite
├── bff/               # Backend for Frontend
├── api_gateway/        # Spring Cloud Gateway
├── ms_usuarios/        # Autenticación y usuarios
├── ms_reportes/        # Reportes de incendios
├── ms_monitoreo/       # Monitoreo geoespacial (PostGIS + SSE)
├── ms_alertas/          # Alertas (email/SMS)
├── ms_integracion/      # MinIO + RabbitMQ
├── postman/            # Postman Collection + ejemplos de comunicación entre servicios
├── tests/              # Código de pruebas unitarias + guía de ejecución
└── docker-compose.yml  # Orquestación de todo el stack
```
