package muni_del_valle.bff.controller;

import muni_del_valle.bff.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_conCredencialesValidas_retornaOkConToken() throws Exception {
        doReturn(ResponseEntity.ok(Map.of("token", "jwt"))).when(authService).login(any());

        mvc.perform(post("/bff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void login_cuandoGatewayRetorna401_propagaStatus() throws Exception {
        doReturn(ResponseEntity.status(401).body("Unauthorized")).when(authService).login(any());

        mvc.perform(post("/bff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_conDatosValidos_retornaOk() throws Exception {
        doReturn(ResponseEntity.ok().build()).when(authService).register(any());

        mvc.perform(post("/bff/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@test.com\",\"password\":\"pass\",\"fullName\":\"Test User\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void register_cuandoEmailDuplicado_retorna409() throws Exception {
        doReturn(ResponseEntity.status(409).body("Conflict")).when(authService).register(any());

        mvc.perform(post("/bff/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dup@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isConflict());
    }
}
