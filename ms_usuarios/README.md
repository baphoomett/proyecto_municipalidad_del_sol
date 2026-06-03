ms_usuarios
===========

Propósito
- Gestionar autenticación, datos personales y permisos/perfiles.
- Permitir reportes anónimos mediante token `guest` para casos urgentes.

Decisiones arquitectónicas
- Microservicio independiente con base de datos PostgreSQL propia (separación por contexto y escalabilidad).
- Persistencia: Spring Data JPA (Repository Pattern implícito) usando `UserRepository` y `RoleRepository`.
- Autenticación: JWT stateless para comunicación con otros servicios/clients.
- Patrones aplicados:
  - Repository Pattern: usando JPA repositories para acceso a datos.
  - Factory Method (implícito): `UserService.createUser` encapsula creación y asignación de roles.
  - Circuit Breaker: no aplica dentro de este servicio actualmente; se recomienda añadir en clientes HTTP que llamen a otros servicios (resilience4j).

Soporte para reportes sin registro
- Endpoint `POST /api/auth/guest` crea un usuario `ROLE_GUEST` con correo temporal y devuelve un token JWT.
- Esto permite que aplicaciones móviles web o ciudadanos obtengan un token rápido y reporten en `ms_alertas` sin registrarse.

Endpoints clave
- `POST /api/auth/register` — registro de usuario.
- `POST /api/auth/login` — login y obtención de JWT.
- `POST /api/auth/guest` — obtiene token guest para reportar sin registro.
- `GET /api/users/me` — detalles del usuario autenticado.

Configuración
- `src/main/resources/application.properties` contiene las propiedades de conexión a Postgres y JWT. Ajustar `spring.datasource.*` según tu entorno.

Ejecución
- Build y run con Maven (no ejecutado ahora por petición del usuario):

```bash
mvn clean package
java -jar target/ms_usuarios-0.0.1-SNAPSHOT.jar
```

Contenedores
- `Dockerfile` incluido para construcción de imagen.

Notas
- Cada microservicio debe tener su propia BD PostgreSQL; usar nombres de bases de datos separados.
- Revisar `jwt.secret` en `application.properties` y cambiar por una clave segura en producción.
