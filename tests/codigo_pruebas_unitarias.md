# Código de las pruebas unitarias - Municipalidad Valle del Sol

Compilación del código fuente real de las pruebas unitarias del backend, organizado por componente (microservicio) y por capa (Service / Controller / Listener). Es el mismo código que está commiteado en cada módulo bajo `src/test/java/...`; este documento solo lo reúne en un solo lugar para revisión.

No incluye los smoke tests `*ApplicationTests.contextLoads` (uno por módulo, generados por Spring Initializr) porque no tienen lógica propia: solo verifican que el contexto de Spring levante.

## Índice

| Componente | Clase de test | Capa | Casos |
|---|---|---|---|
| ms_usuarios | `UserServiceTest` | Service | 4 |
| ms_usuarios | `AuthControllerTest` | Controller (`@WebMvcTest`) | 5 |
| ms_reportes | `ReportServiceTest` | Service | 13 |
| ms_reportes | `ReportControllerTest` | Controller (`@WebMvcTest`) | 6 |
| ms_monitoreo | `FocusFactoryTest` | Factory | 5 |
| ms_monitoreo | `FocusServiceTest` | Service | 8 |
| ms_alertas | `AlertServiceTest` | Service | 2 |
| ms_alertas | `AlertListenerTest` | Listener (RabbitMQ) | 3 |
| ms_integracion | `MinioServiceTest` | Service | 3 |
| ms_integracion | `IntegrationControllerTest` | Controller (`@WebMvcTest`) | 4 |

---

## 1. ms_usuarios

### 1.1 `UserServiceTest` (Service)
`ms_usuarios/src/test/java/muni_del_valle/ms_usuarios/ms_usuarios/service/UserServiceTest.java`

```java
package muni_del_valle.ms_usuarios.ms_usuarios.service;

import muni_del_valle.ms_usuarios.ms_usuarios.model.Role;
import muni_del_valle.ms_usuarios.ms_usuarios.model.User;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.RoleRepository;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_usuarioExistente_deberiaRetornarUserDetailsConSusRoles() {
        User user = new User();
        user.setEmail("admin@municipalidad.cl");
        user.setPassword("hashed-password");
        user.setRoles(new HashSet<>(java.util.List.of(new Role("ROLE_ADMIN"))));

        when(userRepository.findByEmail("admin@municipalidad.cl")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("admin@municipalidad.cl");

        assertThat(result.getUsername()).isEqualTo("admin@municipalidad.cl");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_usuarioInexistente_deberiaLanzarUsernameNotFoundException() {
        when(userRepository.findByEmail("noexiste@municipalidad.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("noexiste@municipalidad.cl"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void createUser_conRolExistente_deberiaReutilizarElRolYGuardarElUsuarioConPasswordCodificado() {
        User user = new User();
        user.setEmail("nuevo@municipalidad.cl");
        user.setPassword("plain-password");

        Role existingRole = new Role("ROLE_USER");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(existingRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(user, "ROLE_USER");

        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getRoles()).containsExactly(existingRole);
        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_conRolInexistente_deberiaCrearElRolYAsignarloAlUsuario() {
        User user = new User();
        user.setEmail("guest@anonymous.local");
        user.setPassword("random-password");

        when(passwordEncoder.encode("random-password")).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_GUEST")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(user, "ROLE_GUEST");

        assertThat(result.getRoles()).extracting("name").containsExactly("ROLE_GUEST");
        verify(roleRepository, times(1)).save(any(Role.class));
    }
}
```

### 1.2 `AuthControllerTest` (Controller, `@WebMvcTest`)
`ms_usuarios/src/test/java/muni_del_valle/ms_usuarios/ms_usuarios/controller/AuthControllerTest.java`

```java
package muni_del_valle.ms_usuarios.ms_usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_usuarios.ms_usuarios.dto.AuthRequest;
import muni_del_valle.ms_usuarios.ms_usuarios.dto.RegisterRequest;
import muni_del_valle.ms_usuarios.ms_usuarios.security.JwtUtil;
import muni_del_valle.ms_usuarios.ms_usuarios.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void register_conDatosValidos_deberiaRetornar200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("nuevo@municipalidad.cl");
        req.setPassword("password123");
        req.setFullName("Nuevo Usuario");

        when(userService.createUser(any(muni_del_valle.ms_usuarios.ms_usuarios.model.User.class), eq("ROLE_USER")))
                .thenReturn(new muni_del_valle.ms_usuarios.ms_usuarios.model.User());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void register_conEmailInvalido_deberiaRetornar400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("no-es-un-email");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_credencialesValidas_deberiaRetornar200ConToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("admin@municipalidad.cl");
        req.setPassword("password123");

        UserDetails ud = new User("admin@municipalidad.cl", "x", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userService.loadUserByUsername("admin@municipalidad.cl")).thenReturn(ud);
        when(jwtUtil.generateToken("admin@municipalidad.cl", "ROLE_ADMIN")).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void login_credencialesInvalidas_deberiaRetornar401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("admin@municipalidad.cl");
        req.setPassword("password-incorrecta");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guest_deberiaCrearUsuarioInvitadoYRetornarToken() throws Exception {
        muni_del_valle.ms_usuarios.ms_usuarios.model.User created = new muni_del_valle.ms_usuarios.ms_usuarios.model.User();
        created.setEmail("guest+123@anonymous.local");

        when(userService.createUser(any(muni_del_valle.ms_usuarios.ms_usuarios.model.User.class), eq("ROLE_GUEST")))
                .thenReturn(created);
        when(jwtUtil.generateToken(anyString())).thenReturn("fake-guest-token");

        mockMvc.perform(post("/api/auth/guest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-guest-token"));
    }
}
```

---

## 2. ms_reportes

### 2.1 `ReportServiceTest` (Service)
`ms_reportes/src/test/java/muni_del_valle/ms_reportes/ms_reportes/service/ReportServiceTest.java`

```java
package muni_del_valle.ms_reportes.ms_reportes.service;

import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.EventType;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import muni_del_valle.ms_reportes.ms_reportes.repository.EventRepository;
import muni_del_valle.ms_reportes.ms_reportes.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WebhookService webhookService;

    @Mock
    private AlertWebhookService alertWebhookService;

    @InjectMocks
    private ReportService reportService;

    private CreateReportRequest buildRequest() {
        CreateReportRequest req = new CreateReportRequest();
        req.setReporterEmail("test@municipalidad.cl");
        req.setLatitude(-36.8201);
        req.setLongitude(-73.0444);
        req.setDescription("Foco de incendio detectado");
        req.setSeverity("ALTA");
        req.setIncidentType("FORESTAL");
        return req;
    }

    @Test
    void createReport_deberiaGuardarReporteYEventoInicial() {
        CreateReportRequest req = buildRequest();

        Report saved = ReportFactory.createReport(req);
        saved.setId(1L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.ACTIVO);
        assertThat(result.getSeverity()).isEqualTo("ALTA");
        assertThat(result.getIncidentType()).isEqualTo("FORESTAL");

        verify(reportRepository, times(1)).save(any(Report.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createReport_deberiaNotificarAMonitoreoYAlertas() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(2L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        reportService.createReport(req);

        verify(webhookService, times(1)).notifyMonitor(any(Report.class));
        verify(alertWebhookService, times(1)).notifyAlertas(any(Report.class));
    }

    @Test
    void createReport_noDeberiaFallarSiNotificacionMonitorLanzaExcepcion() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(3L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());
        doThrow(new RuntimeException("ms_monitoreo no disponible"))
                .when(webhookService).notifyMonitor(any(Report.class));

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    void createReport_noDeberiaFallarSiNotificacionAlertasLanzaExcepcion() {
        CreateReportRequest req = buildRequest();
        Report saved = ReportFactory.createReport(req);
        saved.setId(4L);
        when(reportRepository.save(any(Report.class))).thenReturn(saved);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());
        doThrow(new RuntimeException("ms_alertas no disponible"))
                .when(alertWebhookService).notifyAlertas(any(Report.class));

        Report result = reportService.createReport(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4L);
    }

    @Test
    void listReports_sinFiltro_deberiaRetornarTodos() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report(), new Report()));
        when(reportRepository.findAll(pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.empty(), pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(reportRepository, times(1)).findAll(pageable);
        verify(reportRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listReports_conFiltroValido_deberiaFiltrarPorStatus() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report()));
        when(reportRepository.findByStatus(ReportStatus.ACTIVO, pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.of("ACTIVO"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(reportRepository, times(1)).findByStatus(ReportStatus.ACTIVO, pageable);
    }

    @Test
    void listReports_conFiltroInvalido_deberiaRetornarTodosSinFallar() {
        Pageable pageable = mock(Pageable.class);
        Page<Report> page = new PageImpl<>(List.of(new Report()));
        when(reportRepository.findAll(pageable)).thenReturn(page);

        Page<Report> result = reportService.listReports(Optional.of("ESTADO_INEXISTENTE"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(reportRepository, times(1)).findAll(pageable);
    }

    @Test
    void addEvent_reporteExistente_deberiaGuardarEventoYActualizarStatusADespachado() {
        Report report = new Report();
        report.setId(5L);
        report.setStatus(ReportStatus.ACTIVO);

        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        req.setPayload("Equipo despachado al lugar");

        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Event> result = reportService.addEvent(5L, req);

        assertThat(result).isPresent();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.EN_COMBATE);
        verify(reportRepository, times(1)).save(report);
    }

    @Test
    void addEvent_eventoClosed_deberiaActualizarStatusAExtinguido() {
        Report report = new Report();
        report.setId(6L);
        report.setStatus(ReportStatus.EN_COMBATE);

        CreateEventRequest req = new CreateEventRequest();
        req.setType("CLOSED");

        when(reportRepository.findById(6L)).thenReturn(Optional.of(report));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        reportService.addEvent(6L, req);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.EXTINGUIDO);
    }

    @Test
    void addEvent_reporteInexistente_deberiaRetornarOptionalVacio() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Event> result = reportService.addEvent(99L, new CreateEventRequest());

        assertThat(result).isEmpty();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateStatus_reporteExistenteYEstadoValido_deberiaActualizar() {
        Report report = new Report();
        report.setId(7L);
        report.setStatus(ReportStatus.ACTIVO);

        when(reportRepository.findById(7L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Report> result = reportService.updateStatus(7L, "CONTROLADO");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(ReportStatus.CONTROLADO);
    }

    @Test
    void updateStatus_estadoInvalido_deberiaRetornarOptionalVacio() {
        Report report = new Report();
        report.setId(8L);
        report.setStatus(ReportStatus.ACTIVO);

        when(reportRepository.findById(8L)).thenReturn(Optional.of(report));

        Optional<Report> result = reportService.updateStatus(8L, "ESTADO_QUE_NO_EXISTE");

        assertThat(result).isEmpty();
        verify(reportRepository, never()).save(any());
    }

    @Test
    void updateStatus_reporteInexistente_deberiaRetornarOptionalVacio() {
        when(reportRepository.findById(100L)).thenReturn(Optional.empty());

        Optional<Report> result = reportService.updateStatus(100L, "ACTIVO");

        assertThat(result).isEmpty();
    }
}
```

### 2.2 `ReportControllerTest` (Controller, `@WebMvcTest`)
`ms_reportes/src/test/java/muni_del_valle/ms_reportes/ms_reportes/controller/ReportControllerTest.java`

```java
package muni_del_valle.ms_reportes.ms_reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateEventRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.CreateReportRequest;
import muni_del_valle.ms_reportes.ms_reportes.dto.UpdateStatusRequest;
import muni_del_valle.ms_reportes.ms_reportes.model.Event;
import muni_del_valle.ms_reportes.ms_reportes.model.Report;
import muni_del_valle.ms_reportes.ms_reportes.model.ReportStatus;
import muni_del_valle.ms_reportes.ms_reportes.service.ReportService;
import muni_del_valle.ms_reportes.ms_reportes.service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private UploadService uploadService;

    private Report buildReport(Long id) {
        Report r = new Report();
        r.setId(id);
        r.setReporterEmail("test@municipalidad.cl");
        r.setLatitude(-36.8201);
        r.setLongitude(-73.0444);
        r.setDescription("Foco de incendio detectado");
        r.setStatus(ReportStatus.ACTIVO);
        return r;
    }

    @Test
    void createReport_conDatosValidos_deberiaRetornar201YElReporteCreado() throws Exception {
        CreateReportRequest req = new CreateReportRequest();
        req.setReporterEmail("test@municipalidad.cl");
        req.setLatitude(-36.8201);
        req.setLongitude(-73.0444);
        req.setDescription("Foco de incendio detectado");

        Report saved = buildReport(1L);
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reports/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVO"));
    }

    @Test
    void listReports_sinFiltro_deberiaRetornar200ConPaginaDeReportes() throws Exception {
        Page<Report> page = new PageImpl<>(List.of(buildReport(1L), buildReport(2L)));
        when(reportService.listReports(any(Optional.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void addEvent_reporteExistente_deberiaRetornar200ConElEvento() throws Exception {
        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        req.setPayload("Equipo despachado");

        Event event = new Event();
        event.setId(10L);
        when(reportService.addEvent(eq(5L), any(CreateEventRequest.class))).thenReturn(Optional.of(event));

        mockMvc.perform(post("/api/reports/5/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void addEvent_reporteInexistente_deberiaRetornar404() throws Exception {
        CreateEventRequest req = new CreateEventRequest();
        req.setType("DISPATCHED");
        when(reportService.addEvent(eq(99L), any(CreateEventRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/reports/99/events")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_estadoValido_deberiaRetornar200ConElReporteActualizado() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("CONTROLADO");

        Report updated = buildReport(7L);
        updated.setStatus(ReportStatus.CONTROLADO);
        when(reportService.updateStatus(eq(7L), eq("CONTROLADO"))).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/api/reports/7/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONTROLADO"));
    }

    @Test
    void updateStatus_estadoInvalidoOReporteInexistente_deberiaRetornar404() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus("ESTADO_QUE_NO_EXISTE");

        when(reportService.updateStatus(eq(8L), eq("ESTADO_QUE_NO_EXISTE"))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/reports/8/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

}
```

---

## 3. ms_monitoreo

### 3.1 `FocusFactoryTest` (Factory, sin mocks)
`ms_monitoreo/src/test/java/muni_del_valle/ms_monitoreo/ms_monitoreo/service/FocusFactoryTest.java`

```java
package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusFactoryTest {

    private CreateFocusRequest buildRequest(String wkt, String severity) {
        CreateFocusRequest req = new CreateFocusRequest();
        req.setReportId(1L);
        req.setGeometry(wkt);
        req.setDescription("Foco detectado por sensor satelital");
        req.setSeverity(severity);
        return req;
    }

    @Test
    void createFocus_conWktValidoYSeveridadValida_deberiaCrearFocoConGeometriaYSeveridad() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", "HIGH");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getReportId()).isEqualTo(1L);
        assertThat(focus.getGeometry()).isNotNull();
        assertThat(focus.getGeometry().getGeometryType()).isEqualTo("Point");
        assertThat(focus.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(focus.getDescription()).isEqualTo("Foco detectado por sensor satelital");
    }

    @Test
    void createFocus_conWktInvalido_deberiaLanzarIllegalArgumentException() {
        CreateFocusRequest req = buildRequest("ESTO_NO_ES_WKT", "HIGH");

        assertThatThrownBy(() -> FocusFactory.createFocus(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid geometry WKT");
    }

    @Test
    void createFocus_conSeveridadInvalida_deberiaUsarMediumPorDefecto() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", "SEVERIDAD_QUE_NO_EXISTE");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void createFocus_conSeveridadNula_deberiaUsarMediumPorDefecto() {
        CreateFocusRequest req = buildRequest("POINT(-73.0444 -36.8201)", null);

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void createFocus_sinGeometria_deberiaCrearFocoConGeometriaNula() {
        CreateFocusRequest req = buildRequest(null, "LOW");

        Focus focus = FocusFactory.createFocus(req);

        assertThat(focus.getGeometry()).isNull();
        assertThat(focus.getSeverity()).isEqualTo(Severity.LOW);
    }
}
```

### 3.2 `FocusServiceTest` (Service)
`ms_monitoreo/src/test/java/muni_del_valle/ms_monitoreo/ms_monitoreo/service/FocusServiceTest.java`

```java
package muni_del_valle.ms_monitoreo.ms_monitoreo.service;

import muni_del_valle.ms_monitoreo.ms_monitoreo.dto.CreateFocusRequest;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.Focus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.model.FocusStatus;
import muni_del_valle.ms_monitoreo.ms_monitoreo.repository.FocusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FocusServiceTest {

    @Mock
    private FocusRepository focusRepository;

    @Mock
    private AmqpTemplate amqpTemplate;

    private FocusService focusService;

    @BeforeEach
    void setUp() {
        focusService = new FocusService(focusRepository, amqpTemplate);
    }

    private CreateFocusRequest buildRequest() {
        CreateFocusRequest req = new CreateFocusRequest();
        req.setReportId(10L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco activo");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void createFocus_conDatosValidos_deberiaGuardarYPublicarEnRabbitMQ() {
        CreateFocusRequest req = buildRequest();
        when(focusRepository.save(any(Focus.class))).thenAnswer(inv -> {
            Focus f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        Focus result = focusService.createFocus(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReportId()).isEqualTo(10L);
        verify(focusRepository, times(1)).save(any(Focus.class));
        verify(amqpTemplate, times(1)).convertAndSend(eq("alerts.exchange"), eq("alerts.new"), any(Object.class));
    }

    @Test
    void createFocus_siRabbitMQFalla_noDeberiaPropagarLaExcepcion() {
        CreateFocusRequest req = buildRequest();
        when(focusRepository.save(any(Focus.class))).thenAnswer(inv -> {
            Focus f = inv.getArgument(0);
            f.setId(2L);
            return f;
        });
        doThrow(new RuntimeException("RabbitMQ no disponible"))
                .when(amqpTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Focus result = focusService.createFocus(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void listFocus_sinFiltro_deberiaRetornarTodos() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus(), new Focus()));
        when(focusRepository.findAll(pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.empty(), pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(focusRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listFocus_conFiltroValido_deberiaFiltrarPorStatus() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus()));
        when(focusRepository.findByStatus(FocusStatus.VERIFIED, pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.of("VERIFIED"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(focusRepository, times(1)).findByStatus(FocusStatus.VERIFIED, pageable);
    }

    @Test
    void listFocus_conFiltroInvalido_deberiaRetornarTodosSinFallar() {
        Pageable pageable = mock(Pageable.class);
        Page<Focus> page = new PageImpl<>(List.of(new Focus()));
        when(focusRepository.findAll(pageable)).thenReturn(page);

        Page<Focus> result = focusService.listFocus(Optional.of("ESTADO_QUE_NO_EXISTE"), pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(focusRepository, times(1)).findAll(pageable);
    }

    @Test
    void getById_focoExistente_deberiaRetornarloEnvueltoEnOptional() {
        Focus focus = new Focus();
        focus.setId(5L);
        when(focusRepository.findById(5L)).thenReturn(Optional.of(focus));

        Optional<Focus> result = focusService.getById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
    }

    @Test
    void getById_focoInexistente_deberiaRetornarOptionalVacio() {
        when(focusRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Focus> result = focusService.getById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void subscribe_deberiaRetornarUnSseEmitterNoNulo() {
        SseEmitter emitter = focusService.subscribe();

        assertThat(emitter).isNotNull();
    }
}
```

---

## 4. ms_alertas

### 4.1 `AlertServiceTest` (Service)
`ms_alertas/src/test/java/muni_del_valle/ms_monitoreo/ms_alertas/service/AlertServiceTest.java`

```java
package muni_del_valle.ms_monitoreo.ms_alertas.service;

import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private SmsSender smsSender;

    @InjectMocks
    private AlertService alertService;

    private CreateAlertRequest buildRequest() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(1L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco de incendio detectado");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void handleNewFocus_deberiaEnviarEmailYSms() {
        CreateAlertRequest req = buildRequest();

        alertService.handleNewFocus(req);

        verify(emailSender, times(1)).sendAlert(req);
        verify(smsSender, times(1)).sendAlert(req);
    }

    @Test
    void handleNewFocus_siEmailSenderFalla_deberiaPropagarExcepcionYNoEnviarSms() {
        CreateAlertRequest req = buildRequest();
        doThrow(new RuntimeException("SMTP no disponible")).when(emailSender).sendAlert(req);

        assertThatThrownBy(() -> alertService.handleNewFocus(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("SMTP no disponible");

        verify(smsSender, never()).sendAlert(any());
    }
}
```

### 4.2 `AlertListenerTest` (Listener, RabbitMQ)
`ms_alertas/src/test/java/muni_del_valle/ms_monitoreo/ms_alertas/listener/AlertListenerTest.java`

```java
package muni_del_valle.ms_monitoreo.ms_alertas.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_monitoreo.ms_alertas.dto.CreateAlertRequest;
import muni_del_valle.ms_monitoreo.ms_alertas.model.FailedAlert;
import muni_del_valle.ms_monitoreo.ms_alertas.repository.FailedAlertRepository;
import muni_del_valle.ms_monitoreo.ms_alertas.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertListenerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private FailedAlertRepository failedAlertRepository;

    private AlertListener alertListener;

    @BeforeEach
    void setUp() {
        alertListener = new AlertListener(alertService, failedAlertRepository, new ObjectMapper());
    }

    private CreateAlertRequest buildRequest() {
        CreateAlertRequest req = new CreateAlertRequest();
        req.setReportId(7L);
        req.setGeometry("POINT(-73.0444 -36.8201)");
        req.setDescription("Foco activo");
        req.setSeverity("HIGH");
        return req;
    }

    @Test
    void onMessage_conPayloadValido_deberiaDelegarEnAlertService() {
        CreateAlertRequest req = buildRequest();

        alertListener.onMessage(req);

        verify(alertService, times(1)).handleNewFocus(req);
        verify(failedAlertRepository, never()).save(any());
    }

    @Test
    void onMessage_conPayloadNulo_deberiaIgnorarSinLlamarAlertService() {
        alertListener.onMessage(null);

        verify(alertService, never()).handleNewFocus(any());
        verify(failedAlertRepository, never()).save(any());
    }

    @Test
    void onMessage_siAlertServiceFalla_deberiaPersistirFailedAlertConElError() {
        CreateAlertRequest req = buildRequest();
        doThrow(new RuntimeException("ms_alertas: SMTP no disponible")).when(alertService).handleNewFocus(req);

        alertListener.onMessage(req);

        ArgumentCaptor<FailedAlert> captor = ArgumentCaptor.forClass(FailedAlert.class);
        verify(failedAlertRepository, times(1)).save(captor.capture());
        FailedAlert saved = captor.getValue();
        assertThat(saved.getReportId()).isEqualTo(7L);
        assertThat(saved.getError()).isEqualTo("ms_alertas: SMTP no disponible");
        assertThat(saved.getPayload()).contains("\"reportId\":7");
    }
}
```

---

## 5. ms_integracion

### 5.1 `MinioServiceTest` (Service)
`ms_integracion/src/test/java/muni_del_valle/ms_integracion/service/MinioServiceTest.java`

```java
package muni_del_valle.ms_integracion.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioService(minioClient);
        ReflectionTestUtils.setField(minioService, "bucket", "integracion");
    }

    @Test
    void upload_conDatosValidos_deberiaInvocarPutObjectEnMinioClient() throws Exception {
        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

        InputStream data = new ByteArrayInputStream("contenido-de-prueba".getBytes());
        minioService.upload("evidencias/foto1.jpg", data, 19L, "image/jpeg");

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_siMinioClientFalla_deberiaPropagarLaExcepcion() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("MinIO no disponible"));

        InputStream data = new ByteArrayInputStream("x".getBytes());

        assertThatThrownBy(() -> minioService.upload("evidencias/foto2.jpg", data, 1L, "image/jpeg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("MinIO no disponible");
    }

    @Test
    void generatePresignedUrl_conDatosValidos_deberiaRetornarLaUrlDeMinioClient() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.local/integracion/evidencias/foto1.jpg?sig=abc");

        String url = minioService.generatePresignedUrl("evidencias/foto1.jpg", 3600);

        assertThat(url).isEqualTo("https://minio.local/integracion/evidencias/foto1.jpg?sig=abc");
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
```

### 5.2 `IntegrationControllerTest` (Controller, `@WebMvcTest`)
`ms_integracion/src/test/java/muni_del_valle/ms_integracion/controller/IntegrationControllerTest.java`

```java
package muni_del_valle.ms_integracion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import muni_del_valle.ms_integracion.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntegrationController.class)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MinioService minioService;

    @Test
    void presigned_conObjectNameValido_deberiaRetornar200ConLaUrl() throws Exception {
        when(minioService.generatePresignedUrl(eq("evidencias/foto1.jpg"), eq(3600)))
                .thenReturn("https://minio.local/presigned-url");

        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectName", "evidencias/foto1.jpg",
                                "expirySeconds", 3600))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://minio.local/presigned-url"));
    }

    @Test
    void presigned_sinObjectName_deberiaRetornar400() throws Exception {
        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("expirySeconds", 3600))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("objectName is required"));
    }

    @Test
    void presigned_siMinioServiceFalla_deberiaRetornar500() throws Exception {
        when(minioService.generatePresignedUrl(eq("evidencias/foto1.jpg"), eq(3600)))
                .thenThrow(new RuntimeException("MinIO no disponible"));

        mockMvc.perform(post("/api/minio/presigned")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectName", "evidencias/foto1.jpg",
                                "expirySeconds", 3600))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("MinIO no disponible"));
    }

    @Test
    void health_deberiaRetornar200ConStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
```
