# Frontend - Municipalidad Valle del Sol

Aplicación web (React + Vite) para la gestión de reportes de incendios: login, dashboard, mapa de focos, reportes, alertas y panel de administración.

## Módulos del proyecto

Este frontend es parte de un sistema mayor compuesto por:
- `frontend` (este módulo) — SPA en React/Vite: login, dashboard, mapa de focos, reportes, alertas y panel de administración.
- `bff` — Backend for Frontend: API simplificada para el frontend, agrega CORS y valida roles antes de reenviar al gateway.
- `api_gateway` — Punto único de entrada al backend: rutea cada request al microservicio interno correspondiente.
- `ms_usuarios` — Autenticación, datos personales y roles/permisos.
- `ms_reportes` — Gestión de reportes de incendios y su ciclo de vida.
- `ms_monitoreo` — Seguimiento geoespacial de focos activos en tiempo real (SSE).
- `ms_alertas` — Emisión de alertas por email y SMS (simulado) ante nuevos focos.
- `ms_integracion` — Integración con MinIO (evidencia) y RabbitMQ.

## Stack

- React 19 + React Router 7
- Vite 8
- Axios (cliente HTTP)
- Leaflet / react-leaflet (mapa)
- ESLint

## Requisitos previos

- [Node.js](https://nodejs.org/) 18 o superior y npm
- El backend corriendo (al menos el BFF en `http://localhost:8086`), normalmente vía Docker Compose desde la raíz del repositorio

## 1. Instalación

```bash
cd frontend
npm install
```

## 2. Levantar el backend

El frontend consume el **BFF** (`bff`) en `http://localhost:8086` (ver `src/services/api.js`). El BFF a su vez depende del `api_gateway` y de todos los microservicios. La forma más simple de tener todo arriba es usar Docker Compose desde la raíz del proyecto:

```bash
cd ..
docker-compose up -d --build
```

Esto levanta: `ms_usuarios`, `ms_reportes`, `ms_monitoreo`, `ms_alertas`, `ms_integracion`, sus bases PostgreSQL/PostGIS, RabbitMQ, MailHog, `api_gateway` y `bff`.

Verifica que el BFF responda antes de usar el frontend:

```bash
curl http://localhost:8086/actuator/health
```

> Si necesitas apuntar el frontend a otra URL de backend, edita `baseURL` en `frontend/src/services/api.js`.

## 3. Ejecutar el frontend en desarrollo

```bash
cd frontend
npm run dev
```

Vite levanta el servidor de desarrollo (por defecto en `http://localhost:5173`) con hot-reload. Abre esa URL en el navegador.

## 4. Build de producción

```bash
npm run build      # genera la carpeta dist/
npm run preview    # sirve el build localmente para verificarlo
```

## 5. Pruebas

Este proyecto no tiene un framework de tests automatizados configurado (no hay Vitest/Jest/Cypress ni script `test` en `package.json`). La verificación se hace de dos formas:

### 5.1 Análisis estático (lint)

```bash
npm run lint
```

Revisa el código con ESLint (reglas de React Hooks y React Refresh incluidas).

### 5.2 Prueba manual end-to-end

Con el backend levantado y `npm run dev` corriendo, sigue este checklist recorriendo las rutas definidas en `src/App.jsx`:

| Ruta | Página | Qué probar |
|------|--------|------------|
| `/` | Login (`LoginPage`) | Iniciar sesión con credenciales válidas e inválidas; verificar que un login correcto guarde el token y redirija al dashboard |
| `/dashboard` | `DashboardPage` | Acceder solo estando autenticado (`ProtectedRoute`); sin token, debe redirigir a `/` |
| `/reports` | `ReportsPage` | Listar y crear reportes; validar manejo de errores del backend |
| `/map` | `MapPage` | El mapa Leaflet carga y muestra los focos de incendio obtenidos desde `ms_monitoreo` |
| `/alerts` | `AlertsPage` | Se listan las alertas generadas (`ms_alertas`) |
| `/admin` | `AdminPage` | Funciones de administración (gestión de usuarios/roles vía `ms_usuarios`) |

Casos adicionales a cubrir manualmente:
- Logout y expiración/ausencia de token: cualquier ruta protegida sin sesión debe redirigir a `/`.
- Llamadas a la API (pestaña Network del navegador): confirmar que el header `Authorization: Bearer <token>` se envía en cada request autenticada (ver interceptor en `src/services/api.js`).
- Comportamiento ante backend caído: detener `bff` (`docker-compose stop bff`) y verificar que el frontend muestre un error controlado en vez de romperse.

## Estructura relevante

```
src/
├── App.jsx              # Rutas y protección de rutas
├── main.jsx             # Entry point
├── context/AuthContext.jsx
├── services/api.js      # Cliente Axios (baseURL del BFF)
├── components/          # Navbar, Footer
└── pages/                # Login, Dashboard, Reports, Map, Alerts, Admin
```
