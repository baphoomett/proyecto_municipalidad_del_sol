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

Pruebas unitarias
-----------------

Ejecución: `./mvnw test` (Windows: `mvnw.cmd test`) dentro de `ms_usuarios/`.

Resumen de la última ejecución: **9/10 pruebas pasaron**. La única falla (`MsUsuariosApplicationTests.contextLoads`) es una prueba de contexto Spring Boot ya existente en el proyecto (no agregada en esta tarea) que requiere una base PostgreSQL real corriendo (vía `docker-compose`); no es una prueba unitaria y falla por falta de infraestructura, no por un defecto de código.

### `UserServiceTest` (pruebas unitarias con Mockito sobre `UserService`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `loadUserByUsername_usuarioExistente_deberiaRetornarUserDetailsConSusRoles` | Unitaria (mock de `UserRepository`) | Si el email existe, retorna un `UserDetails` con el mismo username, password y las authorities mapeadas desde los roles del usuario | Retornó `UserDetails` con username, password y authority `ROLE_ADMIN` correctos | ✅ Pasó |
| 2 | `loadUserByUsername_usuarioInexistente_deberiaLanzarUsernameNotFoundException` | Unitaria (mock de `UserRepository`) | Si el email no existe, lanza `UsernameNotFoundException` | Se lanzó `UsernameNotFoundException` | ✅ Pasó |
| 3 | `createUser_conRolExistente_deberiaReutilizarElRolYGuardarElUsuarioConPasswordCodificado` | Unitaria (mocks de `RoleRepository`, `UserRepository`, `PasswordEncoder`) | Si el rol ya existe, se reutiliza (no se crea uno nuevo), la password se codifica con el `PasswordEncoder` y el usuario se guarda | Password codificada, rol existente reutilizado, `roleRepository.save` nunca invocado, `userRepository.save` invocado una vez | ✅ Pasó |
| 4 | `createUser_conRolInexistente_deberiaCrearElRolYAsignarloAlUsuario` | Unitaria (mocks de `RoleRepository`, `UserRepository`, `PasswordEncoder`) | Si el rol no existe, se crea uno nuevo (`ROLE_GUEST`) y se asigna al usuario | Rol `ROLE_GUEST` creado y asignado, `roleRepository.save` invocado una vez | ✅ Pasó |

### `AuthControllerTest` (pruebas de slice `@WebMvcTest` sobre `AuthController`, con `UserService`, `AuthenticationManager` y `JwtUtil` mockeados vía `@MockitoBean`)

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `register_conDatosValidos_deberiaRetornar200` | Integración de capa web (MockMvc) | `POST /api/auth/register` con datos válidos retorna `200 OK` | Retornó `200 OK` | ✅ Pasó |
| 2 | `register_conEmailInvalido_deberiaRetornar400` | Integración de capa web (MockMvc) | `POST /api/auth/register` con un email mal formado retorna `400 Bad Request` por validación `@Email` | Retornó `400 Bad Request` | ✅ Pasó |
| 3 | `login_credencialesValidas_deberiaRetornar200ConToken` | Integración de capa web (MockMvc) | Con credenciales válidas, `POST /api/auth/login` retorna `200 OK` con el JWT generado en el cuerpo (`$.token`) | Retornó `200 OK` con `token = "fake-jwt-token"` | ✅ Pasó |
| 4 | `login_credencialesInvalidas_deberiaRetornar401` | Integración de capa web (MockMvc) | Si `AuthenticationManager` lanza `BadCredentialsException`, el endpoint retorna `401 Unauthorized` | Retornó `401 Unauthorized` | ✅ Pasó |
| 5 | `guest_deberiaCrearUsuarioInvitadoYRetornarToken` | Integración de capa web (MockMvc) | `POST /api/auth/guest` crea un usuario `ROLE_GUEST` y retorna `200 OK` con un token en el cuerpo | Retornó `200 OK` con `token = "fake-guest-token"` | ✅ Pasó |

### Prueba preexistente

| # | Caso | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|------|-----------------|---------------------|---------------------|--------|
| 1 | `MsUsuariosApplicationTests.contextLoads` | Smoke test de contexto Spring Boot (`@SpringBootTest`) | El contexto de la aplicación carga correctamente | Falla al crear el `DataSource`: no hay un driver/URL de base de datos configurado para pruebas (requiere PostgreSQL real vía `docker-compose`) | ❌ Falló (requiere infraestructura externa, no es una prueba unitaria) |
