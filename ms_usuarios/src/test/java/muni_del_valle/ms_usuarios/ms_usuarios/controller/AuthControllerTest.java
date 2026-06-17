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
