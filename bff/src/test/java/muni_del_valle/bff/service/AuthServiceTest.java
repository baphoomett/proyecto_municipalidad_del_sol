package muni_del_valle.bff.service;

import muni_del_valle.bff.dto.AuthDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(restTemplate, "http://gateway:8085");
    }

    @Test
    void login_conCredencialesValidas_retornaOkConToken() {
        AuthDto dto = new AuthDto();
        dto.setEmail("user@test.com");
        dto.setPassword("password");

        when(restTemplate.postForEntity(
                eq("http://gateway:8085/api/auth/login"), any(), eq(Object.class)
        )).thenReturn(ResponseEntity.ok(Map.of("token", "jwt-token")));

        ResponseEntity<?> response = authService.login(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_conCredencialesInvalidas_retorna401() {
        AuthDto dto = new AuthDto();
        dto.setEmail("bad@test.com");
        dto.setPassword("wrong");

        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(401).body("Unauthorized"));

        ResponseEntity<?> response = authService.login(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void register_conDatosNuevos_retornaOk() {
        AuthDto dto = new AuthDto();
        dto.setEmail("new@test.com");
        dto.setPassword("password");
        dto.setFullName("Nuevo Usuario");

        when(restTemplate.postForEntity(
                eq("http://gateway:8085/api/auth/register"), any(), eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> response = authService.register(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_conEmailDuplicado_propaga409() {
        AuthDto dto = new AuthDto();
        dto.setEmail("dup@test.com");
        dto.setPassword("password");

        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(409).body("Email already exists"));

        ResponseEntity<?> response = authService.register(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(409);
    }

    @Test
    void register_propagaCuerpoDeRespuestaDelGateway() {
        AuthDto dto = new AuthDto();
        dto.setEmail("x@test.com");
        dto.setPassword("pass");

        Object body = Map.of("id", 42);
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(body));

        ResponseEntity<?> response = authService.register(dto);

        assertThat(response.getBody()).isEqualTo(body);
    }
}
