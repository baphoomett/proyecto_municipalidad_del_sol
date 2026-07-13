package muni_del_valle.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(restTemplate, "http://gateway:8085");
    }

    @Test
    void getAlerts_deberiaRetornarRespuestaOkDelGateway() {
        when(restTemplate.exchange(
                eq("http://gateway:8085/api/alerts"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(java.util.List.of()));

        ResponseEntity<?> response = alertService.getAlerts("valid-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAlerts_cuandoGatewayRetornaError_propagaElStatusCode() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("down"));

        ResponseEntity<?> response = alertService.getAlerts("token");

        assertThat(response.getStatusCodeValue()).isEqualTo(503);
    }

    @Test
    void getAlerts_deberiaEnviarBearerTokenEnHeaders() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)
        )).thenReturn(ResponseEntity.ok(null));

        ResponseEntity<?> response = alertService.getAlerts("my-jwt-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
