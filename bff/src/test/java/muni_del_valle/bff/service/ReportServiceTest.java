package muni_del_valle.bff.service;

import muni_del_valle.bff.dto.ReportDto;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(restTemplate, "http://gateway:8085");
    }

    @Test
    void createReport_retornaRespuesta201DelGateway() {
        ReportDto dto = new ReportDto();
        dto.setDescription("Incendio en sector norte");

        when(restTemplate.postForEntity(
                eq("http://gateway:8085/api/reports"), any(), eq(Object.class)
        )).thenReturn(ResponseEntity.status(201).body(Map.of("id", 1L)));

        ResponseEntity<?> response = reportService.createReport(dto, "tok");

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    void getReports_retornaListaDelGateway() {
        when(restTemplate.exchange(
                eq("http://gateway:8085/api/reports"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        ResponseEntity<?> response = reportService.getReports("tok");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateStatus_retornaRespuestaDelGateway() {
        when(restTemplate.exchange(
                contains("/api/reports/5/status"), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class)
        )).thenReturn(ResponseEntity.ok("CONTROLADO"));

        ResponseEntity<?> response = reportService.updateStatus(5L, "CONTROLADO", "tok");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void extinguishReport_eliminaAlertaYLuegoReporte() {
        when(restTemplate.exchange(
                contains("/api/alerts/by-report/1"), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());
        when(restTemplate.exchange(
                contains("/api/reports/1"), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<?> response = reportService.extinguishReport(1L, "tok");

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(restTemplate).exchange(contains("/api/alerts/by-report/1"), eq(HttpMethod.DELETE), any(), eq(Void.class));
        verify(restTemplate).exchange(contains("/api/reports/1"), eq(HttpMethod.DELETE), any(), eq(Void.class));
    }

    @Test
    void extinguishReport_cuandoBorradoAlertaFalla_igualEliminaReporte() {
        doThrow(new RuntimeException("alert ms down"))
                .when(restTemplate).exchange(contains("alerts"), eq(HttpMethod.DELETE), any(), eq(Void.class));
        when(restTemplate.exchange(
                contains("/api/reports/2"), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<?> response = reportService.extinguishReport(2L, "tok");

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void createReport_propagaCuerpoDeRespuestaDelGateway() {
        ReportDto dto = new ReportDto();
        Object body = Map.of("id", 99, "status", "ACTIVO");
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(201).body(body));

        ResponseEntity<?> response = reportService.createReport(dto, "tok");

        assertThat(response.getBody()).isEqualTo(body);
    }
}
