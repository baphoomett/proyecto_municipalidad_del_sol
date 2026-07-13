package muni_del_valle.ms_usuarios.ms_usuarios.controller;

import muni_del_valle.ms_usuarios.ms_usuarios.model.User;
import muni_del_valle.ms_usuarios.ms_usuarios.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.standaloneSetup(new UserController(userRepository))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User buildUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setFullName("Test User");
        u.setRoles(new HashSet<>());
        return u;
    }

    private void setMockUser(String username) {
        UserDetails ud = new org.springframework.security.core.userdetails.User(
                username, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
    }

    @Test
    void me_usuarioAutenticadoExistente_retornaOkConDatos() throws Exception {
        setMockUser("user@test.com");
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(buildUser("user@test.com")));

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void me_usuarioAutenticadoPeroNoEnBD_retorna404() throws Exception {
        setMockUser("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    void me_sinAutenticacion_retorna401() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_existente_retornaOkConUsuario() throws Exception {
        User user = buildUser("admin@test.com");
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        mvc.perform(get("/api/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void getById_noExistente_retorna404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
